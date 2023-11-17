package tech.finovy.gateway.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import tech.finovy.gateway.config.GatewayConfiguration;
import tech.finovy.gateway.common.constant.Constant;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.context.ConfigurationContext;
import tech.finovy.gateway.context.ConfigurationContextHolder;
import tech.finovy.gateway.context.TraceContextItem;
import tech.finovy.gateway.common.entity.HostItemEntity;
import tech.finovy.gateway.common.entity.SkipUrlItemEntity;
import tech.finovy.gateway.globalfilter.GlobalChainContext;
import tech.finovy.gateway.globalfilter.listener.GlobalAuthListener;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class DispatcherHandlerMethodInterceptor implements MethodInterceptor {
    public static final AtomicLong REQUEST_ATOMIC_LONG = new AtomicLong();
    public static final AtomicInteger REQUEST_ATOMIC_INTEGER_SUB = new AtomicInteger();
    @Autowired
    private Map<String, Map<String, GlobalAuthListener>> globalTokenListeners;
    private ConfigurationContext configurationContext = ConfigurationContextHolder.get();
    @Autowired
    private GatewayConfiguration configuration;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if ("handle".equals(methodInvocation.getMethod().getName()) && methodInvocation.getArguments().length == 1 && methodInvocation.getArguments()[0] instanceof ServerWebExchange) {
            ServerWebExchange exchange = (ServerWebExchange) methodInvocation.getArguments()[0];
            ServerHttpRequest request = exchange.getRequest();
            HttpHeaders headers = request.getHeaders();
            URI uri = request.getURI();
            String path = uri.getRawPath();
            SkipUrlItemEntity urlItem = configurationContext.pathContain(path);
            String hostName = uri.getHost();
            HostItemEntity hostItem = configurationContext.getHostItemEntity(hostName);
            Map<String, GlobalAuthListener> tokenListeners = globalTokenListeners.get(GlobalAuthConstant.AUTH_LISTENER_TYPE);
            if (tokenListeners == null) {
                tokenListeners = globalTokenListeners.get(GlobalAuthConstant.DEFAULT_TOKEN_LISTENER_TYPE);
            }
            String traceId = this.getHeaderValue(request, headers, this.configuration.getTraceHeaderKey(), this.configuration.getTraceQueryKey());
            long requestCount = REQUEST_ATOMIC_LONG.incrementAndGet();
            TraceContextItem contextItem = TraceContextItem.deserialize(traceId);
            if (contextItem.isValid()) {
                // 如果traceId是skywalking的上下文， 那么转接一下
                exchange.getRequest().mutate().header("sw8", traceId);
                // 替换traceId的值
                traceId = contextItem.getTraceId();
                exchange.getRequest().mutate().header(this.configuration.getTraceHeaderKey(), traceId);
            }
            if (StringUtils.isBlank(traceId)) {
                // 使用生成的sw8
                traceId = TraceContext.traceId();
                exchange.getRequest().mutate().header(this.configuration.getTraceHeaderKey(), traceId);
            }
            if (StringUtils.isBlank(traceId) || "N/A".equalsIgnoreCase(traceId)) {
                // gateway自生成
                traceId = configuration.getTraceIdPrefix() + configuration.getStartupTimeMillis() + configuration.getTraceIdAppend() + requestCount;
            }
            String inputToken = getHeaderValue(request, headers, configuration.getHeaderTokenKey(), configuration.getQueryTokenKey());
            String tokenHeaderAppId = getHeaderValue(request, headers, configuration.getHeaderAppKey(), configuration.getQueryAppKey());
            MDC.put(Constant.TRACE_ID, traceId);
            MDC.put(Constant.TOKEN, inputToken);
            GlobalChainContext context = new GlobalChainContext(requestCount, traceId, headers, configuration, headers.getContentType(), hostItem, urlItem, configurationContext.getRemoveHeaders(), tokenListeners);
            context.replaceHeader(configuration.getOutPutAppKey(), configuration.getDefaultApppId());
            context.replaceHeader(configuration.getOutPutAppKey(), hostItem.getAppId());
            context.replaceHeader(configuration.getOutPutAppKey(), tokenHeaderAppId);
            context.replaceHeader(configuration.getOutPutTokenKey(), inputToken);
            context.setToken(inputToken);
            context.setAuthEnable(configuration.isAuthEnable());
            context.setSkipRefreshListener(urlItem.isSkipRefreshListener() || configuration.isSkipRefresh());
            String scheme = uri.getScheme();
            if (skipScheme(scheme)) {
                context.setFilterType("skipScheme");
                context.setSkipScheme(true);
            }
            if (urlItem.isSkipAuth() || !configuration.isAuthEnable()) {
                context.setFilterType("skipPath");
                context.setSkipAuth(true);
            }
            if (skipExtend(path)) {
                context.setFilterType("skipExtend");
                context.setSkipExtend(true);
            }
            context.setForbiddenHost(configuration.isValidatorHost() && hostItem.isExists());
            context.setValidatorToken(configuration.isValidatorToken() || hostItem.isValidatorToken());
            exchange.getAttributes().put(GlobalChainContext.CONTEXT_KEY, context);
        }
        return methodInvocation.proceed();
    }

    private String getHeaderValue(ServerHttpRequest request, HttpHeaders headers, String headerKeys, String queryKey) {
        String value = request.getQueryParams().getFirst(queryKey);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        String[] split = headerKeys.split(",");
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        for (String key : split) {
            value = headers.getFirst(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
            HttpCookie cook = cookies.getFirst(key);
            if (cook != null) {
                return cook.getValue();
            }
        }
        return null;
    }


    private boolean skipExtend(String path) {
        if (path == null) {
            return true;
        }
        int ix = path.lastIndexOf('.');
        if (ix >= 0) {
            String extend = path.substring(ix);
            return Arrays.asList(configuration.getSkipAuthExtend()).contains(extend);
        }
        return false;
    }

    private boolean skipScheme(String scheme) {
        if (scheme == null) {
            log.warn("Scheme is null");
            return true;
        }
        return Arrays.asList(configuration.getSkipAuthScheme()).contains(scheme.toLowerCase());
    }
}
