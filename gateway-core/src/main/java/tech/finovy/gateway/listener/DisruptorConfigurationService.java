package tech.finovy.gateway.listener;

import tech.finovy.framework.disruptor.spi.ProcessInterface;
import tech.finovy.gateway.config.GatewayConfiguration;

public interface DisruptorConfigurationService extends ProcessInterface {
    void setConfiguration(GatewayConfiguration configuration);
}
