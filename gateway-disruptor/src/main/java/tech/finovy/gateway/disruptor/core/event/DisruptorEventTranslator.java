package tech.finovy.gateway.disruptor.core.event;

import com.lmax.disruptor.EventTranslatorThreeArg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;


@Slf4j
public class DisruptorEventTranslator implements EventTranslatorThreeArg<DisruptorEvent, String, Long, DisruptorEvent> {
    @Override
    public void translateTo(DisruptorEvent disruptorEvent, long sequence, String transactionId, Long eventId, DisruptorEvent event) {
        BeanUtils.copyProperties(event, disruptorEvent);
        disruptorEvent.setTransactionId(event.getTransactionId());
        disruptorEvent.setEventId(eventId);
        disruptorEvent.setDisruptorEvent(event.getEvent());
        disruptorEvent.setEventTags(event.getEventTags());
        disruptorEvent.setDisruptorEventType(event.getDisruptorEventType());
        disruptorEvent.setApplication(event.getApplication());
        disruptorEvent.setEventTopic(event.getEventTopic());
    }
}
