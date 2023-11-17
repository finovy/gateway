package tech.finovy.gateway.globalfilter;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.*;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.finovy.gateway.config.GatewayConfiguration;
import tech.finovy.gateway.common.entity.SkipUrlItemEntity;
import tech.finovy.gateway.globalfilter.listener.GlobalAuthListener;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class GlobalChainFilter implements GlobalFilter, Ordered {

    @Autowired
    private GlobalChainEventPushHandler tokenEventPushHandler;
    @Autowired
    private GatewayConfiguration configuration;
    @Autowired
    private BodyInserterFactory bodyInserterFactory;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        GlobalChainContext context = exchange.getAttribute(GlobalChainContext.CONTEXT_KEY);
        if (context == null || context.isSkip()) {
            return chain.filter(exchange);
        }
        if (context.isForbiddenHost()) {
            context.setAuthMessage("Http Host ERROR");
            context.setAuthCode(configuration.getTokenNotExistsCode());
            return forbidden(exchange, context);
        }
        if (context.isValidatorToken()) {
            if (StringUtils.isBlank(context.getToken())) {
                context.setAuthMessage(configuration.getTokenNotExistsMessage());
                context.setAuthCode(configuration.getTokenNotExistsCode());
                return forbidden(exchange, context);
            }
            Map<String, GlobalAuthListener> tokenListeners = context.getAuthListeners();
            for (GlobalAuthListener listener : tokenListeners.values()) {
                listener.refreshToken(context);
                if (context.isForbidden()) {
                    context.removeHeader(configuration.getOutPutTokenKey());
                    return forbidden(exchange, context);
                }
            }
        }
        context.setFilterType("send");
        return chain.filter(exchange);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, GlobalChainContext context) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.valueOf(configuration.getForbiddenCode()));
        final String sw8 = exchange.getRequest().getHeaders().getFirst("sw8");
        ContextCarrierRef contextCarrierRef = new ContextCarrierRef();
        CarrierItemRef items = contextCarrierRef.items();
        while (items.hasNext()) {
            items = items.next();
            if ("sw8".equals(items.getHeadKey())) {
                items.setHeadValue(sw8);
            }
        }
        final ServerHttpRequest request = exchange.getRequest();
        String operationName = request.getMethod() + ":" + request.getPath();
        SpanRef spanRef = Tracer.createEntrySpan(operationName, contextCarrierRef);
        String traceId = TraceContext.traceId();
        if (StringUtils.isBlank(traceId) || "N/A".equalsIgnoreCase(traceId)) {
            traceId = context.getTraceId();
        } else {
            // 上报
            ActiveSpan.info(context.getAuthMessage());
            Tracer.stopSpan();
            spanRef.asyncFinish();
        }
        byte[] bytes = ("{\"code\":" + context.getAuthCode() + ",\"message\":\"" + context.getAuthMessage() + "\"" + ",\"traceId\":\"" + traceId + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        context.setFilterType("forbidden");
        return response.writeWith(Flux.just(buffer))
                .then(Mono.fromRunnable(() -> {
                            SkipUrlItemEntity urlItemEntity = context.getUrlItem();
                            if (urlItemEntity.isSkipLog()) {
                                return;
                            }
                            tokenEventPushHandler.requestPush(exchange, context);
                            MDC.clear();
                        })
                )
                ;
    }

    @Override
    public int getOrder() {
        return configuration.getAuthFilterOrder();
    }
}
