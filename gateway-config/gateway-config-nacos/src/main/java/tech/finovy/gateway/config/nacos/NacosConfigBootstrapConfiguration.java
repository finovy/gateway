package tech.finovy.gateway.config.nacos;

import com.alibaba.cloud.nacos.NacosConfigManager;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tech.finovy.gateway.config.nacos.context.NacosContext;
import tech.finovy.gateway.config.nacos.listener.InitApplicationListener;
import tech.finovy.gateway.config.nacos.listener.NacosConfigDefinitionListener;
import tech.finovy.gateway.disruptor.core.DisruptorEventConfiguration;
import tech.finovy.gateway.disruptor.provider.DefaultDisruptorEngineProvider;
import tech.finovy.gateway.disruptor.spi.DisruptorEngine;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = {"tech.finovy"})
@ConditionalOnProperty(name = "spring.cloud.nacos.config.enabled", matchIfMissing = true)
@AutoConfigureAfter(NacosConfigDefinitionListener.class)
public class NacosConfigBootstrapConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NacosContext nacosContext(ApplicationContext context, NacosConfigManager nacosConfigManager, List<NacosConfigDefinitionListener> listeners) {
        if (context.getParent() != null && BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context.getParent(), NacosContext.class).length > 0) {
            return BeanFactoryUtils.beanOfTypeIncludingAncestors(context.getParent(), NacosContext.class);
        }
        NacosContext nacosContext = new NacosContext(nacosConfigManager.getConfigService(), listeners);
        return nacosContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationListener InitApplicationListener(ApplicationContext context) {
        if (context.getParent() != null && BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context.getParent(), InitApplicationListener.class).length > 0) {
            return BeanFactoryUtils.beanOfTypeIncludingAncestors(context.getParent(), InitApplicationListener.class);
        }
        InitApplicationListener initApplicationListener = new InitApplicationListener();
        return initApplicationListener;
    }

    @Bean
    @ConditionalOnMissingBean(value = DisruptorEngine.class, search = SearchStrategy.CURRENT)
    public DisruptorEngine disruptorEngine(ApplicationContext context, DisruptorEventConfiguration configuration) {
        if (context.getParent() != null && BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context.getParent(), DisruptorEngine.class).length > 0) {
            return BeanFactoryUtils.beanOfTypeIncludingAncestors(context.getParent(), DisruptorEngine.class);
        }
        return new DefaultDisruptorEngineProvider(configuration);
    }
}
