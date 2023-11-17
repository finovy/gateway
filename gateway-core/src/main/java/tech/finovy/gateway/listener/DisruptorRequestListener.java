package tech.finovy.gateway.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import tech.finovy.framework.disruptor.core.event.DisruptorEvent;
import tech.finovy.gateway.config.GatewayConfiguration;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.common.entity.RequestEvent;
import tech.finovy.gateway.globalfilter.listener.AbstractDisruptorChainEventListener;
import tech.finovy.gateway.remote.RemoteLogPush;

import java.util.List;

@Slf4j
@Component
public class DisruptorRequestListener extends AbstractDisruptorChainEventListener {

    private GatewayConfiguration configuration;

    public void setConfiguration(GatewayConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getType() {
        return GlobalAuthConstant.AUTH_REQUEST_TYPE;
    }

    @Override
    public void onEvent(DisruptorEvent event, int handlerId) {
        if (!(event.getEvent() instanceof RequestEvent)) {
            log.warn("Type error,event:{}", event.getEvent().getClass());
            return;
        }
        RequestEvent requestEvent = (RequestEvent) event.getEvent();
        MultiValueMap<String, String> requestHeader = requestEvent.getRequestHeaders();
        List<String> acceptEncoding = requestHeader.get(HttpHeaders.CONTENT_ENCODING);
        String acceptType = compType(acceptEncoding);
        byte[] responseTxt = uncompresss(acceptType, requestEvent.getRequestBody());
        if (configuration.isTraceDebug()) {
            log.info("TraceID:{},Request,Method:{},Scheme:{}://{}:{}{}  ,Content-Type:{} ,params:{} ,header:{}",
                    requestEvent.getTransactionId(), requestEvent.getHttpMethod(),
                    requestEvent.getScheme(), requestEvent.getHost(), requestEvent.getPort(),
                    requestEvent.getUrl(), requestEvent.getContentType(), responseTxt == null ? "" : new String(responseTxt), requestEvent.getRequestHeaders());
        }
        RemoteLogPush.push(requestEvent);
    }
}
