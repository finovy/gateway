package tech.finovy.gateway.listener.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tech.finovy.gateway.common.configuration.ExceptionConfiguration;
import tech.finovy.gateway.common.configuration.GatewayConfiguration;
import tech.finovy.gateway.common.constant.Constant;
import tech.finovy.gateway.common.exception.ExceptionEntity;

import javax.net.ssl.SSLHandshakeException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class JsonExceptionHandler implements ErrorWebExceptionHandler {

    public static final AtomicInteger REQUEST_ATOMIC_INTEGER_ERROR = new AtomicInteger();
    private static final ThreadLocal<Map<String, Object>> EXCEPTION_HANDLER_RESULT = new ThreadLocal<>();
    private static final String HTTP_STATUS = "httpStatus";
    private static final String HTTP_BODY = "body";
    private List<HttpMessageReader<?>> messageReaders = Collections.emptyList();
    private List<HttpMessageWriter<?>> messageWriters = Collections.emptyList();
    private List<ViewResolver> viewResolvers = Collections.emptyList();
    @Autowired
    private ExceptionEventPushHandler exceptionEventPushHandler;
    @Autowired
    private ExceptionConfiguration exceptionConfiguration;
    @Autowired
    private GatewayConfiguration configuration;

    public void setMessageReaders(List<HttpMessageReader<?>> messageReaders) {
        Assert.notNull(messageReaders, "'messageReaders' must not be null");
        this.messageReaders = messageReaders;
    }

    public void setViewResolvers(List<ViewResolver> viewResolvers) {
        this.viewResolvers = viewResolvers;
    }

    public void setMessageWriters(List<HttpMessageWriter<?>> messageWriters) {
        Assert.notNull(messageWriters, "'messageWriters' must not be null");
        this.messageWriters = messageWriters;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ExceptionEntity exceptionEntity = new ExceptionEntity(HttpStatus.INTERNAL_SERVER_ERROR, exceptionConfiguration.getStatusCodeInternalServerError(), exceptionConfiguration.getInternalServerError());
        exceptionSwitch(exceptionEntity, ex);
        Map<String, Object> result = new HashMap<>(2, 1);
        result.put(HTTP_STATUS, exceptionEntity.getHttpStatus());
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String traceId = getHeaderValue(request, headers, configuration.getTraceHeaderKey(), configuration.getTraceQueryKey());
        if (StringUtils.isBlank(traceId)) {
            traceId = configuration.getTraceIdPrefix() + configuration.getStartupTimeMillis() + configuration.getTraceIdAppend() + "E" + REQUEST_ATOMIC_INTEGER_ERROR.incrementAndGet();
        } else {
            traceId = traceId + "-E" + REQUEST_ATOMIC_INTEGER_ERROR.incrementAndGet();
        }
        MDC.put(Constant.TRACE_ID, traceId);
        String msg = "{\"code\":" + exceptionEntity.getHttpCode() + ",\"message\": \"" + exceptionEntity.getBody() + "\",\"traceId\":\"" + traceId + "\"}";
        result.put(HTTP_BODY, msg);
        try {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            exceptionEventPushHandler.postException(route, traceId, exceptionEntity, request, ex);
        } catch (Exception e) {
            log.error(e.toString());
        }
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        EXCEPTION_HANDLER_RESULT.set(result);
        ServerRequest newRequest = ServerRequest.create(exchange, this.messageReaders);
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse).route(newRequest)
                .switchIfEmpty(Mono.error(ex))
                .flatMap(handler -> handler.handle(newRequest))
                .flatMap(response -> write(exchange, response));

    }

    private String getHeaderValue(ServerHttpRequest request, HttpHeaders headers, String headerKey, String queryKey) {
        String value = request.getQueryParams().getFirst(queryKey);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        value = headers.getFirst(headerKey);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        HttpCookie cook = cookies.getFirst(headerKey);
        if (cook == null) {
            return null;
        }
        return cook.getValue();
    }

    private void exceptionSwitch(ExceptionEntity exceptionEntity, Throwable ex) {
        if (ex instanceof NotFoundException) {
            exceptionEntity.refresh(HttpStatus.NOT_FOUND, exceptionConfiguration.getStatusCodeNotFound(), exceptionConfiguration.getPageNotFoundMessage());
            return;
        }
        if (ex instanceof UnknownHostException) {
            UnknownHostException unknownHostException = (UnknownHostException) ex;
            exceptionEntity.setHttpCode(exceptionConfiguration.getStatusCodeInternalServerError());
            if (exceptionConfiguration.isThrowErrorToPage()) {
                exceptionEntity.setBody(unknownHostException.toString());
            }
        }
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            exceptionEntity.refresh(responseStatusException.getStatusCode(), exceptionConfiguration.getStatusCodeNotFound(), responseStatusException.getMessage());
            return;
        }
        if (ex instanceof UnsupportedMediaTypeException) {
            UnsupportedMediaTypeException unsupportedMediaTypeException = (UnsupportedMediaTypeException) ex;
            exceptionEntity.setHttpCode(exceptionConfiguration.getStatusCodeInternalServerError());
            if (exceptionConfiguration.isThrowErrorToPage()) {
                exceptionEntity.setBody(unsupportedMediaTypeException.getMessage());
            }
            return;
        }
        if (ex instanceof SSLHandshakeException) {
            SSLHandshakeException responseStatusException = (SSLHandshakeException) ex;
            if (exceptionConfiguration.isThrowErrorToPage()) {
                exceptionEntity.setBody(responseStatusException.getMessage());
            }
            return;
        }
        String exName = ex.getClass().getTypeName();
        if ("io.netty.channel.AbstractChannel$AnnotatedConnectException".equals(exName)) {
            exceptionEntity.setHttpCode(exceptionConfiguration.getStatusCodeInternalServerError());
            if (exceptionConfiguration.isThrowErrorToPage()) {
                exceptionEntity.setBody(ex.getMessage());
            }
            return;
        }
        if (exceptionConfiguration.isThrowErrorToPage()) {
            exceptionEntity.setBody(ex.getMessage());
        }
        log.warn(ex.toString());
    }

    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> result = EXCEPTION_HANDLER_RESULT.get();
        EXCEPTION_HANDLER_RESULT.remove();
        return ServerResponse.status((HttpStatus) result.get(HTTP_STATUS))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(result.get(HTTP_BODY)));
    }

    private Mono<? extends Void> write(ServerWebExchange exchange, ServerResponse response) {
        MDC.clear();
        return response.writeTo(exchange, new ResponseContext());
    }

    private class ResponseContext implements ServerResponse.Context {

        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return JsonExceptionHandler.this.messageWriters;
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return JsonExceptionHandler.this.viewResolvers;
        }
    }
}
