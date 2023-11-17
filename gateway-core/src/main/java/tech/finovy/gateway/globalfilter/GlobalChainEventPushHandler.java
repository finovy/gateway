package tech.finovy.gateway.globalfilter;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import tech.finovy.framework.disruptor.core.event.DisruptorEvent;
import tech.finovy.framework.disruptor.spi.DisruptorEngine;
import tech.finovy.gateway.config.GatewayConfiguration;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.context.ConfigurationContext;
import tech.finovy.gateway.context.ConfigurationContextHolder;
import tech.finovy.gateway.common.entity.RequestEvent;
import tech.finovy.gateway.common.entity.ResponseEvent;
import tech.finovy.gateway.common.entity.SkipUrlItemEntity;

import java.net.InetSocketAddress;
import java.net.URI;

@Slf4j
@Component
public class GlobalChainEventPushHandler {
    @Autowired
    private GatewayConfiguration configuration;
    @Autowired
    private DisruptorEngine disruptorEngine;
    private ConfigurationContext configurationContext = ConfigurationContextHolder.get();

    public void requestPush(ServerWebExchange exchange, GlobalChainContext context) {
        ServerHttpRequest request = exchange.getRequest();
        if (context == null) {
            log.error("GlobalAuthContext IS NULL");
            return;
        }
        SkipUrlItemEntity urlItemEntity = context.getUrlItem();
        if (urlItemEntity.isSkipLog()) {
            return;
        }
        RequestEvent event;
        if (urlItemEntity.isSkipRequest() || context.getRequestBody() == null) {
            event = new RequestEvent(null);
        } else {
            event = new RequestEvent(context.getRequestBody());
        }
        setRequestProp(context.getTraceId(), event, request);
        event.setTransactionId(context.getTraceId());
        event.setTimestamp(System.currentTimeMillis());

        DisruptorEvent disruptorEvent = new DisruptorEvent();
        disruptorEvent.setDisruptorEvent(event);
        disruptorEvent.setEventId(context.getRequestCount());
        disruptorEvent.setTransactionId(context.getTraceId());
        disruptorEvent.setDisruptorEventType(GlobalAuthConstant.AUTH_REQUEST_TYPE);
        disruptorEngine.post(disruptorEvent);
    }

    public void responsePush(ServerWebExchange exchange, GlobalChainContext context) {
        if (context == null) {
            log.error("GlobalAuthContext is NULL");
            return;
        }
        SkipUrlItemEntity urlItem = context.getUrlItem();
        if (urlItem.isSkipLog()) {
            return;
        }
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = null;
        if (route != null) {
            routeId = route.getId();
        }
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();
        request.getHeaders();
        ResponseEvent responseEvent = new ResponseEvent();
        responseEvent.setFilterType(context.getFilterType());

        responseEvent.setSpends(context.getSpendsTime());
        responseEvent.setTimestamp(System.currentTimeMillis());
        responseEvent.setBody(context.getResponseBody());
        HttpStatusCode st = response.getStatusCode();
        if (st != null) {
            responseEvent.setStatusCode(st.toString());
        }
        responseEvent.setApplication(configuration.getApplicationName());
        responseEvent.setResponseHeaders(response.getHeaders());
        responseEvent.setRquestHeaders(request.getHeaders());
        responseEvent.setRouteId(routeId);
        responseEvent.setTransactionId(context.getTraceId());
        DisruptorEvent disruptorEvent = new DisruptorEvent();
        disruptorEvent.setDisruptorEvent(responseEvent);
        disruptorEvent.setEventId(context.getRequestCount());
        disruptorEvent.setTransactionId(context.getTraceId());
        disruptorEvent.setDisruptorEventType(GlobalAuthConstant.AUTH_RESPONSE_TYPE);
        disruptorEngine.post(disruptorEvent);
    }

    private void setRequestProp(String traceId, RequestEvent event, ServerHttpRequest request) {
        if (request == null) {
            return;
        }
        URI uri = request.getURI();
        event.setRquestHeaders(request.getHeaders());
        event.setHttpMethod(request.getMethod().name());
        InetSocketAddress address = request.getRemoteAddress();
        if (address != null) {
            event.setRemoteAddress(address.toString());
        }
        event.setUrl(request.getPath().toString());
        event.setHost(uri.getHost());
        event.setScheme(uri.getScheme());
        event.setPort(uri.getPort());
        event.setApplication(configuration.getApplicationName());
        event.setTransactionId(traceId);
    }
}
