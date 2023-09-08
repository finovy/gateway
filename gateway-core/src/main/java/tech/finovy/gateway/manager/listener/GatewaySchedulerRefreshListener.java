package tech.finovy.gateway.manager.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.finovy.gateway.common.configuration.ConversionUtil;
import tech.finovy.gateway.common.configuration.GatewayConfiguration;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.config.nacos.listener.AbstractSchedulerRefreshListener;

@Slf4j
@Component
public class GatewaySchedulerRefreshListener extends AbstractSchedulerRefreshListener {

    @Autowired
    private GatewayConfiguration globalAuthConfiguration;

    @Override
    public void trigger(long trigger) {

    }

    @Override
    public void startup() {
        long i = System.currentTimeMillis() - GlobalAuthConstant.STARTUP_TIME_MILLIS;
        String code = ConversionUtil.encode(i, 32);
        globalAuthConfiguration.setStartupTimeMillis(code);
        log.info("Gateway Startup:{}", code);
    }
}
