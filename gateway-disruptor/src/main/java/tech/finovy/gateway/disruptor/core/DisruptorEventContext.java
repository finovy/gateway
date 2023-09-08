package tech.finovy.gateway.disruptor.core;

import com.lmax.disruptor.RingBuffer;
import lombok.Getter;
import lombok.Setter;
import tech.finovy.gateway.disruptor.core.event.DisruptorEvent;

@Getter
@Setter
public class DisruptorEventContext {
    private RingBuffer<DisruptorEvent> ringBuffer;
}
