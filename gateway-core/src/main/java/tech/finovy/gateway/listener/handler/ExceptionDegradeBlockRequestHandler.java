package tech.finovy.gateway.listener.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;
import tech.finovy.gateway.common.constant.Constant;
import tech.finovy.gateway.common.context.ConfigurationContext;
import tech.finovy.gateway.common.context.ConfigurationContextHolder;
import tech.finovy.gateway.common.entity.DegradeEntity;
import tech.finovy.gateway.common.exception.DegradeLogEntity;
import tech.finovy.gateway.common.exception.WebClientBuilderService;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionDegradeBlockRequestHandler implements BlockRequestHandler {
    @Autowired
    private ExceptionEventPushHandler exceptionEventPushHandler;
    private ConfigurationContext configurationContext = ConfigurationContextHolder.get();
    @Autowired
    private WebClientBuilderService webClientBuilderService;

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String traceId = exchange.getAttribute(Constant.TRACE_ID);
        String routeID = "";
        if (route != null && route.getId() != null) {
            routeID = route.getId();
        }
        DegradeEntity degradeEntity = configurationContext.getDegradeEntity(routeID);
        if (ex instanceof ParamFlowException) {
            if (StringUtils.isBlank(degradeEntity.getParamFlowApi())) {
                return createJson(exchange, degradeEntity.getParamFlowCode(), degradeEntity.getParamFlowMessage(), routeID, traceId, ex);
            } else {
                return api(exchange, degradeEntity.getParamFlowCode(), degradeEntity.getParamFlowMessage(), degradeEntity.getParamFlowApi(), routeID, traceId, ex);
            }
        }
        if (ex instanceof DegradeException) {
            if (StringUtils.isBlank(degradeEntity.getDegradeApi())) {
                return createJson(exchange, degradeEntity.getDegradeCode(), degradeEntity.getDegradeMessage(), routeID, traceId, ex);
            } else {
                return api(exchange, degradeEntity.getDegradeCode(), degradeEntity.getDegradeMessage(), degradeEntity.getDegradeApi(), routeID, traceId, ex);
            }
        }
        if (ex instanceof SystemBlockException) {
            if (StringUtils.isBlank(degradeEntity.getSystemBlockApi())) {
                return createJson(exchange, degradeEntity.getSystemBlockCode(), degradeEntity.getSystemBlockMessage(), routeID, traceId, ex);
            } else {
                return api(exchange, degradeEntity.getSystemBlockCode(), degradeEntity.getSystemBlockMessage(), degradeEntity.getSystemBlockApi(), routeID, traceId, ex);
            }
        }
        if (ex instanceof PrematureCloseException) {
            return createJson(exchange, degradeEntity.getDegradeCode(), degradeEntity.getDegradeMessage(), routeID, traceId, ex);
        }
        if (ex instanceof FlowException) {
            return createJson(exchange, degradeEntity.getDegradeCode(), degradeEntity.getDegradeMessage(), routeID, traceId, ex);
        }
        if (ex instanceof AuthorityException) {
            return createJson(exchange, degradeEntity.getDegradeCode(), degradeEntity.getDegradeMessage(), routeID, traceId, ex);
        }
        return createJson(exchange, degradeEntity.getDegradeCode(), degradeEntity.getDegradeMessage(), routeID, traceId, ex);
    }

    private Mono<ServerResponse> createJson(ServerWebExchange exchange, int code, String msg, String routeId, String traceId, Throwable ex) {
        DegradeLogEntity degradeLogEntity = new DegradeLogEntity(code, msg);
        exceptionEventPushHandler.postDegrade(routeId, traceId, degradeLogEntity, exchange.getRequest(), ex);
        return ServerResponse.status(code).contentType(MediaType.APPLICATION_JSON).body(fromValue("{\"code\":" + code + ",\"message\": \"" + msg + "\",\"routeID\": \"" + routeId + "\",\"traceId\":\"" + traceId + "\"}"));
    }

    private Mono<ServerResponse> api(ServerWebExchange exchange, int code, String msg, String api, String routeId, String traceId, Throwable ex) {
        DegradeLogEntity degradeLogEntity = new DegradeLogEntity(code, msg);
        degradeLogEntity.setDegradeApi(api);
        exceptionEventPushHandler.postDegrade(routeId, traceId, degradeLogEntity, exchange.getRequest(), ex);
        return webClientBuilderService.choiceAction(exchange, api, traceId, routeId);
    }
}
