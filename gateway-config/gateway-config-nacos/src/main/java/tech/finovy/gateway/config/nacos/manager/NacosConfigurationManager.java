package tech.finovy.gateway.config.nacos.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import tech.finovy.gateway.common.chain.ChainSortUtil;
import tech.finovy.gateway.config.nacos.listener.SchedulerRefreshListener;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration
@Component
public class NacosConfigurationManager {
    private final AtomicLong schedulerAtomicLong = new AtomicLong();
    private final Map<String, SchedulerRefreshListener> schedulerRefreshListeners = new LinkedHashMap<>();
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(5, 10, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
    @Value("${gateway.nacos.scheduled.initial-delay:30}")
    private int initialDelay;
    @Value("${spring.application.name:}")
    private String application;
    @Value("${gateway.nacos.scheduled.period:10}")
    private int period;

    @Bean
    @Primary
    public Map<String, SchedulerRefreshListener> configurationRefreshListeners(List<SchedulerRefreshListener> listeners) {
        schedulerRefreshListeners.putAll(ChainSortUtil.singleChainListenerSort(listeners));
        return schedulerRefreshListeners;
    }

    @Bean
    @Primary
    public CommandLineRunner configurationDefinitionCommandLineRunner(ConfigurableApplicationContext applicationContext) {
        return strings -> {
            try {
                log.info("{} Startup------------------------", application);
                for (SchedulerRefreshListener entry : schedulerRefreshListeners.values()) {
                    if (entry.isAsync()) {
                        threadPool.execute(entry::startup);
                        continue;
                    }
                    entry.startup();
                }
                ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
                scheduledThreadPool.scheduleAtFixedRate(this::schedulerTrigger, initialDelay, period, TimeUnit.SECONDS);
                log.info("{} Self-Check Scheduler InitialDelay:{},Period:{}", application, initialDelay, period);
            } catch (Exception e) {
                log.error(e.toString());
            }
        };
    }


    public void schedulerTrigger() {
        long cc = schedulerAtomicLong.incrementAndGet();
        for (SchedulerRefreshListener entry : schedulerRefreshListeners.values()) {
            if (entry.isAsync()) {
                threadPool.execute(() -> entry.trigger(cc));
                continue;
            }
            entry.trigger(cc);
        }
    }
}
