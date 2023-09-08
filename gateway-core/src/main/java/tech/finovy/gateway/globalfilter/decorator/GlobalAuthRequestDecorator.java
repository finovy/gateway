package tech.finovy.gateway.globalfilter.decorator;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.finovy.gateway.common.configuration.GatewayConfiguration;
import tech.finovy.gateway.globalfilter.GlobalChainContext;
import tech.finovy.gateway.globalfilter.GlobalChainEventPushHandler;
import tech.finovy.gateway.globalfilter.listener.GlobalAuthListener;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class GlobalAuthRequestDecorator extends ServerHttpRequestDecorator {

    private final GlobalChainContext context;
    private final ServerWebExchange exchange;
    private final GatewayConfiguration configuration;
    private final GlobalChainEventPushHandler globalChainEventPushHandler;

    public GlobalAuthRequestDecorator(ServerWebExchange exchange, GatewayConfiguration configuration, GlobalChainEventPushHandler globalChainEventPushHandler, GlobalChainContext context) {
        super(exchange.getRequest());
        this.context = context;
        this.exchange = exchange;
        this.globalChainEventPushHandler = globalChainEventPushHandler;
        this.configuration = configuration;
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders header = context.getHeaders();
        long contentLength = header.getContentLength();
        if (contentLength > 0) {
            header.setContentLength(contentLength);
        } else {
            header.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
        }
        return header;
    }

    @Override
    public Flux<DataBuffer> getBody() {
        ServerHttpRequest request = exchange.getRequest();
        return request.getBody().flatMap(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);
            context.setRequestBody(bytes);
            globalChainEventPushHandler.requestPush(exchange, context);
            Map<String, GlobalAuthListener> tokenListeners = context.getAuthListeners();
            for (GlobalAuthListener listener : tokenListeners.values()) {
                listener.decode(context);
            }
            Flux<DataBuffer> defer = Flux.defer(() -> {
                DataBuffer buffer = new DefaultDataBufferFactory().wrap(context.getRequestBody());
                DataBufferUtils.retain(buffer);
                return Mono.just(buffer);
            });
            return defer;
        }).switchIfEmpty(Mono.defer(() -> {
            if (request.getQueryParams().isEmpty()) {
                globalChainEventPushHandler.requestPush(exchange, context);
                return Mono.empty();
            }
            context.setRequestBody(JSON.toJSON(request.getQueryParams()).toString().getBytes(StandardCharsets.UTF_8));
            globalChainEventPushHandler.requestPush(exchange, context);
            Map<String, GlobalAuthListener> tokenListeners = context.getAuthListeners();
            for (GlobalAuthListener listener : tokenListeners.values()) {
                listener.decode(context);
            }
            DataBuffer buffer = new DefaultDataBufferFactory().wrap(context.getRequestBody());
            DataBufferUtils.retain(buffer);
            return Mono.just(buffer);
        }));
    }

    @Override
    public MultiValueMap<String, HttpCookie> getCookies() {
        return super.getCookies();
    }
}
