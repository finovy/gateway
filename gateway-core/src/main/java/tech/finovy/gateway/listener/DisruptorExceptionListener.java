package tech.finovy.gateway.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.finovy.framework.disruptor.core.event.DisruptorEvent;
import tech.finovy.framework.disruptor.core.listener.AbstractDisruptorListener;
import tech.finovy.gateway.config.GatewayConfiguration;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.common.entity.ExceptionEvent;
import tech.finovy.gateway.remote.RemoteLogPush;

@Slf4j
@Component
public class DisruptorExceptionListener extends AbstractDisruptorListener implements DisruptorConfigurationService {
    private GatewayConfiguration configuration;

    public void setConfiguration(GatewayConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getType() {
        return GlobalAuthConstant.AUTH_POST_EXCEPTION_TYPE;
    }

    @Override
    public void onEvent(DisruptorEvent event, int handlerId) {
        if (event.getEvent() instanceof ExceptionEvent) {
            RemoteLogPush.push((ExceptionEvent) event.getEvent());
        }
    }
}
