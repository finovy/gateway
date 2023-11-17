package tech.finovy.gateway.listener.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import tech.finovy.framework.disruptor.core.event.DisruptorEvent;
import tech.finovy.framework.disruptor.spi.DisruptorEngine;
import tech.finovy.gateway.config.ExceptionConfiguration;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.common.entity.DegradeEvent;
import tech.finovy.gateway.common.entity.ExceptionEvent;
import tech.finovy.gateway.exception.DegradeLogEntity;
import tech.finovy.gateway.exception.ExceptionEntity;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ExceptionEventPushHandler {
    private final AtomicInteger exceptionAtomicInteger = new AtomicInteger();
    private final AtomicInteger degradeAtomicInteger = new AtomicInteger();
    @Autowired
    private ExceptionConfiguration exceptionConfiguration;
    @Autowired
    private DisruptorEngine disruptorEngine;

    public void postDegrade(String routeId, String traceId, DegradeLogEntity degradeLogEntity, ServerHttpRequest request, Throwable ex) {
        int deCount = degradeAtomicInteger.incrementAndGet();
        URI uri = request.getURI();
        if (routeId != null) {
            log.warn("[Degrade] Count:{} ,TraceID:{},RouteID:{},Status:{}, Method:{},Scheme {}://{}{}  Header:{} ,QueryParams:{},Exception:{},Message:{}",
                    deCount, traceId, routeId, degradeLogEntity.getCode(), request.getMethod(), uri.getScheme(), uri.getHost(),
                    uri.getPath(), request.getHeaders(),
                    request.getQueryParams(), ex.getClass().getTypeName(), degradeLogEntity.getMsg());
        } else {
            log.warn("[Degrade] Count:{},TraceID:{} ,Status:{}, Method:{},Scheme {}://{}{}  Header:{} ,QueryParams:{},Exception:{},Message:{}",
                    deCount, traceId, degradeLogEntity.getCode(), request.getMethod(), uri.getScheme(), uri.getHost(),
                    uri.getPath(), request.getHeaders(),
                    request.getQueryParams(), ex.getClass().getTypeName(), degradeLogEntity.getMsg());
        }
        DegradeEvent event = new DegradeEvent(degradeLogEntity.getCode(), degradeLogEntity.getMsg(), routeId, traceId);
        event.setExceptionMsg(ex.getMessage());
        event.setExceptionType(ex.getClass().getTypeName());
        event.setDegradeApi(degradeLogEntity.getDegradeApi());
        event.setCount(deCount);
        event.setTimestamp(System.currentTimeMillis());
        event.setTransactionId(traceId);
        event.setRouteId(routeId);
        event.setRequestBody(request.getQueryParams().toString().getBytes(StandardCharsets.UTF_8));

        event.setExceptionMsg(ex.toString());

        event.setRquestHeaders(request.getHeaders());
        event.setHost(uri.getHost());
        event.setUrl(uri.getPath());
        event.setHttpMethod(request.getMethod().name());
        event.setScheme(uri.getScheme());
        InetSocketAddress address = request.getRemoteAddress();
        if (address != null) {
            event.setRemoteAddress(address.toString());
        }
        event.setApplication(exceptionConfiguration.getApplicationName());
        DisruptorEvent disruptorEvent = new DisruptorEvent();
        disruptorEvent.setDisruptorEvent(event);
        disruptorEvent.setEventId(deCount);
        disruptorEvent.setTransactionId(traceId);
        disruptorEvent.setDisruptorEventType(GlobalAuthConstant.AUTH_POST_DEGRADE_TYPE);
        disruptorEngine.post(disruptorEvent);
    }

    public void postException(Route route, String traceId, ExceptionEntity exceptionEntity, ServerHttpRequest request, Throwable ex) {
        URI uri = request.getURI();
        int exCount = exceptionAtomicInteger.incrementAndGet();
        if (route != null) {
            log.error("[GlobalException] Count:{} ,TraceID:{},RouteID:{},Status:{}, Method:{},Scheme {}://{}{}  Header:{} ,QueryParams:{},Exception:{},Message:{}",
                    exCount, traceId, route.getId(), exceptionEntity.getHttpStatus(), request.getMethod(), uri.getScheme(), uri.getHost(),
                    uri.getPath(), request.getHeaders(),
                    request.getQueryParams(), ex.getClass().getTypeName(), exceptionEntity.getBody());
        } else {
            log.error("[GlobalException] Count:{},TraceID:{} ,Status:{}, Method:{},Scheme {}://{}{}  Header:{} ,QueryParams:{},Exception:{},Message:{}",
                    exCount, traceId, exceptionEntity.getHttpStatus(), request.getMethod(), uri.getScheme(), uri.getHost(),
                    uri.getPath(), request.getHeaders(),
                    request.getQueryParams(), ex.getClass().getTypeName(), exceptionEntity.getBody());
        }
        ExceptionEvent exceptionEvent = new ExceptionEvent();
        exceptionEvent.setCount(exCount);
        exceptionEvent.setTimestamp(System.currentTimeMillis());
        exceptionEvent.setTransactionId(traceId);
        if (route != null) {
            exceptionEvent.setRouteId(route.getId());
        }
        exceptionEvent.setRequestBody(request.getQueryParams().toString().getBytes(StandardCharsets.UTF_8));
        exceptionEvent.setErrMsg(ex.toString());
        exceptionEvent.setStatusCode(exceptionEntity.getHttpStatus().toString());
        exceptionEvent.setTransactionId(traceId);
        exceptionEvent.setRquestHeaders(request.getHeaders());
        exceptionEvent.setHost(uri.getHost());
        exceptionEvent.setUrl(uri.getPath());
        exceptionEvent.setExceptionType(ex.getClass().getTypeName());
        exceptionEvent.setHttpMethod(request.getMethod().name());
        exceptionEvent.setScheme(uri.getScheme());
        InetSocketAddress address = request.getRemoteAddress();
        if (address != null) {
            exceptionEvent.setRemoteAddress(address.toString());
        }
        exceptionEvent.setApplication(exceptionConfiguration.getApplicationName());
        DisruptorEvent disruptorEvent = new DisruptorEvent();
        disruptorEvent.setDisruptorEvent(exceptionEvent);
        disruptorEvent.setEventId(exCount);
        disruptorEvent.setTransactionId(traceId);
        disruptorEvent.setDisruptorEventType(GlobalAuthConstant.AUTH_POST_EXCEPTION_TYPE);
        disruptorEngine.post(disruptorEvent);
    }


}
