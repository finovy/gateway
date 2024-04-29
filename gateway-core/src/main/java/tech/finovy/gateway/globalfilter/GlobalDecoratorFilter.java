package tech.finovy.gateway.globalfilter;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.webflux.WebFluxSkyWalkingTraceContext;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tech.finovy.gateway.config.GatewayConfiguration;
import tech.finovy.gateway.common.entity.SkipUrlItemEntity;
import tech.finovy.gateway.globalfilter.decorator.GlobalAuthRequestDecorator;
import tech.finovy.gateway.globalfilter.decorator.GlobalAuthResponseDecorator;
import tech.finovy.gateway.globalfilter.listener.GlobalAuthListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CLIENT_RESPONSE_ATTR;

@Slf4j
@Component
public class GlobalDecoratorFilter implements GlobalFilter, Ordered {
    private final AtomicLong errorCount = new AtomicLong();
    @Autowired
    private GatewayConfiguration configuration;
    @Autowired
    private GlobalChainEventPushHandler tokenEventPushHandler;
    @Autowired
    private Map<String, Map<String, GlobalAuthListener>> globalTokenListeners;

    @Override
    public int getOrder() {
        return -2;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return post(exchange, chain, exchange.getAttribute(GlobalChainContext.CONTEXT_KEY));
    }

    private Mono<Void> post(ServerWebExchange exchange, GatewayFilterChain chain, GlobalChainContext context) {
        String traceId = WebFluxSkyWalkingTraceContext.traceId(exchange);
        log.info("POST current traceid:{}", traceId);
        GlobalAuthResponseDecorator responseDecorator = new GlobalAuthResponseDecorator(exchange, configuration, tokenEventPushHandler);
        if (configuration.isChainSkip()) {
            return skipPost(exchange, chain, context);
        }
        MediaType contentType = context.getRequestContentType();
        if (contentType == null) {
            return authPost(exchange, chain, context, responseDecorator);
        }
        if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
            return skipPost(exchange, chain, context);
        }
        return authPost(exchange, chain, context, responseDecorator);
    }

    private Mono<Void> authPost(ServerWebExchange exchange, GatewayFilterChain chain, GlobalChainContext context, GlobalAuthResponseDecorator responseDecorator) {
        GlobalAuthRequestDecorator requestDecorator = new GlobalAuthRequestDecorator(exchange, configuration, tokenEventPushHandler, context);
        ServerWebExchange mutateExchange = exchange.mutate().request(requestDecorator).response(responseDecorator).build();
        return chain
                .filter(mutateExchange)
                .doOnSuccess((aVoid) -> refresh(context))
                .doOnError(throwable -> log.error("OnError count:{},ErrMsg:{}", errorCount.incrementAndGet(), throwable.getMessage()))
                .doFinally(s -> MDC.clear());
    }

    private Mono<Void> skipPost(ServerWebExchange exchange, GatewayFilterChain chain, GlobalChainContext context) {
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
        HttpHeaders header = context.getHeaders();
        if (header != null) {
            for (Map.Entry<String, List<String>> eache : header.entrySet()) {
                List<String> headerList = eache.getValue();
                if (headerList == null) {
                    continue;
                }
                requestBuilder.header(eache.getKey(), headerList.toArray(new String[headerList.size()]));
            }
        }
        ServerHttpRequest httpRequest = requestBuilder.build();
        ServerWebExchange exchangeBuild = exchange
                .mutate()
                .request(httpRequest)
                .build();
        return chain.filter(exchangeBuild).doOnSuccess(
                (aVoid) -> {
                    SkipUrlItemEntity urlItemEntity = context.getUrlItem();
                    if (!urlItemEntity.isSkipLog()) {
                        tokenEventPushHandler.requestPush(exchange, context);
                    }
                    refresh(context);
                }
        ).doOnError(throwable -> log.error(throwable.getMessage())).doFinally(s -> MDC.clear()).doOnCancel(() -> cleanupCancel(exchange));
    }

    private void refresh(GlobalChainContext context) {
        if (context.isSkipRefreshListener() || context.getAuthListeners() == null) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            for (GlobalAuthListener listener : context.getAuthListeners().values()) {
                listener.refresh(context);
            }
        });
    }

    private void cleanupCancel(ServerWebExchange exchange) {
        ClientResponse clientResponse = exchange.getAttribute(CLIENT_RESPONSE_ATTR);
        if (clientResponse != null) {
            clientResponse.bodyToMono(Void.class).subscribe();
        }
        MDC.clear();
    }
}
