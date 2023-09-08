package tech.finovy.gateway.config.nacos.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

@Slf4j
public class NacosLoggingListener implements GenericApplicationListener {

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        Class<?> type = resolvableType.getRawClass();
        if (type != null) {
            return ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(type);
        }
        return false;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        log.info("NacosLoggingListener");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 21;
    }

}
