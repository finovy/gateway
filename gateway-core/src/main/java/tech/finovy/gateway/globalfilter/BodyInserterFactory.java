package tech.finovy.gateway.globalfilter;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tech.finovy.gateway.config.GatewayConfiguration;

import java.util.List;

@Slf4j
@Component
public class BodyInserterFactory {
    private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
    @Autowired
    private GlobalChainEventPushHandler globalChainEventPushHandler;
    @Autowired
    private GatewayConfiguration configuration;

    public BodyInserter createBodyInserter(ServerWebExchange exchange, GlobalChainContext context) {
        ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
        if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(context.getRequestContentType())) {
            return createStringBodyInserter(exchange, serverRequest);
        }
        if (MediaType.APPLICATION_JSON.isCompatibleWith(context.getRequestContentType())) {
            return createJsonBodyInserter(exchange, serverRequest);
        }
        if (MediaType.TEXT_PLAIN.includes(context.getRequestContentType())) {
            return createTextBodyInserter(exchange, serverRequest);
        }
        if (configuration.isTraceDebug() && context.getRequestContentType() != null) {
            ServerHttpRequest request = exchange.getRequest();
            log.info("TraceId:{},ContentType:{},Path:{} ,QueryParams:{}", context.getTraceId(), context.getRequestContentType(), request.getPath(), request.getQueryParams());
        }
        return createObjectBodyInserter(exchange, serverRequest);
    }

    private BodyInserter createObjectBodyInserter(ServerWebExchange exchange, ServerRequest serverRequest) {
        Mono<Object> modifiedBody = serverRequest.bodyToMono(Object.class)
                .flatMap(Mono::just);
        return BodyInserters.fromPublisher(modifiedBody, Object.class);
    }

    private BodyInserter createTextBodyInserter(ServerWebExchange exchange, ServerRequest serverRequest) {
        Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
                .flatMap(Mono::just);
        return BodyInserters.fromPublisher(modifiedBody, String.class);
    }

    private BodyInserter createStringBodyInserter(ServerWebExchange exchange, ServerRequest serverRequest) {
        Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
                .flatMap(Mono::just);
        return BodyInserters.fromPublisher(modifiedBody, String.class);
    }

    private BodyInserter createJsonBodyInserter(ServerWebExchange exchange, ServerRequest serverRequest) {
        Mono<JSONObject> modifiedBody = serverRequest.bodyToMono(JSONObject.class)
                .flatMap(Mono::just);
        return BodyInserters.fromPublisher(modifiedBody, JSONObject.class);
    }

}
