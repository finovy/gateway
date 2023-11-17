package tech.finovy.gateway.globalfilter.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.finovy.framework.disruptor.core.event.DisruptorEvent;
import tech.finovy.framework.disruptor.core.listener.AbstractDisruptorListener;
import tech.finovy.gateway.config.GatewayConfiguration;
import tech.finovy.gateway.listener.DisruptorConfigurationService;

@Slf4j
@Component
public class DefaultDisruptorListener extends AbstractDisruptorListener implements DisruptorConfigurationService {

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
