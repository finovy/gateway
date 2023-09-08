package tech.finovy.gateway.globalfilter.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.finovy.gateway.common.configuration.GatewayConfiguration;
import tech.finovy.gateway.disruptor.core.event.DisruptorEvent;
import tech.finovy.gateway.disruptor.core.listener.AbstractDisruptorListener;
import tech.finovy.gateway.listener.DisrptorConfigurationService;

@Slf4j
@Component
public class DefaultDisruptorListener extends AbstractDisruptorListener implements DisrptorConfigurationService {

    private GatewayConfiguration configuration;

    public void setConfiguration(GatewayConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getType() {
        return "DEFAULT";
    }

    @Override
    public void onEvent(DisruptorEvent event, int handlerId) {
        log.info("--------------------------------");
    }
}
