package tech.finovy.gateway.disruptor.provider;

import lombok.extern.slf4j.Slf4j;
import tech.finovy.gateway.common.loader.LoadLevel;
import tech.finovy.gateway.common.loader.Scope;
import tech.finovy.gateway.disruptor.core.DisruptorEventConstant;
import tech.finovy.gateway.disruptor.core.event.DisruptorEvent;
import tech.finovy.gateway.disruptor.core.listener.AbstractDisruptorListener;

@Slf4j
@LoadLevel(name = "defaultDisruptorClearListener", order = 1, scope = Scope.SINGLETON)
public class DefaultDisruptorClearListener extends AbstractDisruptorListener {
    @Override
    public void onEvent(DisruptorEvent event, int handlerId) {
        if (configuration.isDebug()) {
            log.info("DisruptorEventType:{},application:{},eventTopic:{},eventTags:{},handlerId:{},transactionId:{},eventId:{}", event.getDisruptorEventType(), event.getApplication(), event.getEventTopic(), event.getEventTags(), handlerId, event.getTransactionId(), event.getEventId());
        }
    }

    @Override
    public String getType() {
        return DisruptorEventConstant.SYS_GLOBAL_CLEAR_LISTENER_TYPE;
    }
}
