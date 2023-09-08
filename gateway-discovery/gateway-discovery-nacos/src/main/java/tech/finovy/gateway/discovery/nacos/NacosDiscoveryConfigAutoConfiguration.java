package tech.finovy.gateway.discovery.nacos;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.loadbalancer.config.LoadBalancerCacheAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tech.finovy.gateway.discovery.nacos.subcribe.NacosCacheSubscribe;
import tech.finovy.gateway.disruptor.core.DisruptorEventConfiguration;
import tech.finovy.gateway.disruptor.provider.DefaultDisruptorEngineProvider;
import tech.finovy.gateway.disruptor.spi.DisruptorEngine;

@Slf4j
@EnableCaching
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = {"tech.finovy"})
@AutoConfigureAfter(LoadBalancerCacheAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.nacos.discovery.enabled", matchIfMissing = true)
public class NacosDiscoveryConfigAutoConfiguration {
    @Autowired
    private CacheManager cacheManager;

    @Bean
    @ConditionalOnMissingBean(value = NacosCacheSubscribe.class, search = SearchStrategy.CURRENT)
    public NacosCacheSubscribe nacosCacheSubcribe(ApplicationContext context) {
        if (context.getParent() != null && BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context.getParent(), NacosCacheSubscribe.class).length > 0) {
            return BeanFactoryUtils.beanOfTypeIncludingAncestors(context.getParent(), NacosCacheSubscribe.class);
        }
        return new NacosCacheSubscribe(cacheManager);
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
