package tech.finovy.gateway.globalfilter;

import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.result.view.ViewResolver;
import tech.finovy.gateway.common.chain.ChainSortUtil;
import tech.finovy.gateway.common.configuration.GatewayConfiguration;
import tech.finovy.gateway.disruptor.spi.DisruptorEngine;
import tech.finovy.gateway.disruptor.spi.ProcessInterface;
import tech.finovy.gateway.globalfilter.listener.GlobalAuthListener;
import tech.finovy.gateway.listener.DisrptorConfigurationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Configuration
public class GlobalChainFilterManager {
    private static final Map<String, Map<String, GlobalAuthListener>> DISRUPTOR_EVENT_FILTERS = new HashMap<>();
    private static final Map<String, Map<String, DisrptorConfigurationService>> DISRUPTOR_CONFIGURATION_SERVICE = new HashMap<>();
    @Autowired
    private GatewayConfiguration configuration;
    @Autowired
    private DisruptorEngine disruptorEngine;

    @Primary
    @Bean
    public Map<String, Map<String, GlobalAuthListener>> globalTokenListeners(Map<String, GlobalAuthListener> filters) {
        DISRUPTOR_EVENT_FILTERS.putAll(ChainSortUtil.multiChainListenerSort(filters));
        return DISRUPTOR_EVENT_FILTERS;
    }

    @Primary
    @Bean
    public Map<String, Map<String, DisrptorConfigurationService>> disrptorConfigurationService(List<DisrptorConfigurationService> filters) {
        DISRUPTOR_CONFIGURATION_SERVICE.putAll(ChainSortUtil.multiChainListenerSort(filters));
        List<ProcessInterface> processInterfaces = new ArrayList<>();
        for (DisrptorConfigurationService filter : filters) {
            filter.setConfiguration(configuration);
            processInterfaces.add(filter);
        }
        disruptorEngine.addProcessInterfaces(processInterfaces);
        return DISRUPTOR_CONFIGURATION_SERVICE;
    }


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler(List<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer) {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }
}
