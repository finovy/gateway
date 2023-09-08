package tech.finovy.gateway.disruptor.spi;


import tech.finovy.gateway.common.chain.ChainListener;
import tech.finovy.gateway.disruptor.core.DisruptorEventConfiguration;
import tech.finovy.gateway.disruptor.core.event.DisruptorEvent;

import java.io.Serializable;

public interface ProcessInterface<T extends Serializable> extends ChainListener {
    void onEvent(DisruptorEvent<T> event, int handlerId);

    void setDisruptorEventConfiguration(DisruptorEventConfiguration configuration);
}
