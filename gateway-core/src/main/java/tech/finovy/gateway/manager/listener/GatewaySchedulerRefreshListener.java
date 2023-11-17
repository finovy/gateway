package tech.finovy.gateway.manager.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.finovy.framework.common.core.listener.AbstractShardingEngineStartupRefreshListener;
import tech.finovy.gateway.common.util.ConversionUtil;
import tech.finovy.gateway.config.GatewayConfiguration;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;

@Slf4j
@Component
public class GatewaySchedulerRefreshListener extends AbstractShardingEngineStartupRefreshListener {

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
