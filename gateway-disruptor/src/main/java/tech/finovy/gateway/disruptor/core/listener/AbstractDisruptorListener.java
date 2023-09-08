package tech.finovy.gateway.disruptor.core.listener;


import tech.finovy.gateway.common.chain.AbstractChainListener;
import tech.finovy.gateway.disruptor.core.DisruptorEventConfiguration;
import tech.finovy.gateway.disruptor.spi.ProcessInterface;

public abstract class AbstractDisruptorListener extends AbstractChainListener implements ProcessInterface {
    protected String type;
    protected DisruptorEventConfiguration configuration;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setDisruptorEventConfiguration(DisruptorEventConfiguration configuration) {
        this.configuration = configuration;
    }


}
