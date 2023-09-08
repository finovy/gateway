package tech.finovy.gateway.listener;

import tech.finovy.gateway.common.configuration.GatewayConfiguration;
import tech.finovy.gateway.disruptor.spi.ProcessInterface;

public interface DisrptorConfigurationService extends ProcessInterface {
    void setConfiguration(GatewayConfiguration configuration);
}
