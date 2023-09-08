package tech.finovy.gateway.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import tech.finovy.gateway.common.configuration.GatewayConfiguration;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.common.entity.ResponseEvent;
import tech.finovy.gateway.disruptor.core.event.DisruptorEvent;
import tech.finovy.gateway.globalfilter.listener.AbstractDisruptorChainEventListener;
import tech.finovy.gateway.remote.RemoteLogPush;

import java.util.List;

@Slf4j
@Component
public class DisruptorResponseListener extends AbstractDisruptorChainEventListener {

    private GatewayConfiguration configuration;

    public void setConfiguration(GatewayConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getType() {
        return GlobalAuthConstant.AUTH_RESPONSE_TYPE;
    }

    @Override
    public void onEvent(DisruptorEvent event, int handlerId) {
        if (!(event.getEvent() instanceof ResponseEvent)) {
            log.warn("Type error，event:{}", event.getEvent().getClass());
            return;
        }
        ResponseEvent responseEvent = (ResponseEvent) event.getEvent();
        MultiValueMap<String, String> responseHeader = responseEvent.getResponseHeaders();
        List<String> responseEncoding = responseHeader.get(HttpHeaders.CONTENT_ENCODING);
        String responseType = compType(responseEncoding);
        MultiValueMap<String, String> requestHeader = responseEvent.getRequestHeaders();
        List<String> acceptEncoding = requestHeader.get(HttpHeaders.ACCEPT_ENCODING);
        String acceptType = compType(acceptEncoding);
        byte[] responseTxt = uncompresss(responseType, responseEvent.getBody());
        if (configuration.isTraceDebug()) {
            log.info("TraceID:{},Response,type:{},Status:{},Spends:{} ms,RouteID:{},ResponseContentType:{},AcceptType:{},ResponseType:{},Body:{},headers:{}",
                    responseEvent.getTransactionId(), responseEvent.getFilterType(),
                    responseEvent.getStatusCode(), responseEvent.getSpends(), responseEvent.getRouteId(), responseEvent.getContentType(), acceptType, responseType,
                    responseTxt == null ? "" : new String(responseTxt), responseEvent.getResponseHeaders());
        }
        RemoteLogPush.push(responseEvent);
    }

}
