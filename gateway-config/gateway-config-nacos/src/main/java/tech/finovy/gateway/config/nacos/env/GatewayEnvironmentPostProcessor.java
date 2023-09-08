package tech.finovy.gateway.config.nacos.env;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
public class GatewayEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.info("--------------postProcessEnvironment-------------------------------------");
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
