package tech.finovy.gateway.disruptor;


import tech.finovy.gateway.common.loader.EnhancedServiceLoader;
import tech.finovy.gateway.disruptor.core.DisruptorEventConfiguration;
import tech.finovy.gateway.disruptor.spi.DisruptorEngine;

public class DisruptorEngineFactory {
    private DisruptorEngineFactory() {
    }

    private static DisruptorEngine INSTANCE = null;

    public static DisruptorEngine getDisruptorEngine() {
        return getDisruptorEngine(new DisruptorEventConfiguration());
    }

    public static DisruptorEngine getDisruptorEngine(DisruptorEventConfiguration configuration) {
        if (INSTANCE == null) {
            Class<?>[] arrType = {DisruptorEventConfiguration.class};
            Object[] args = {configuration};
            INSTANCE = EnhancedServiceLoader.load(DisruptorEngine.class, DisruptorConstant.DISRUPTOR, arrType, args);
        }
        return INSTANCE;
    }
}
