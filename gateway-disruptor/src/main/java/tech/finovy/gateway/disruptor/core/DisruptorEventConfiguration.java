package tech.finovy.gateway.disruptor.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class DisruptorEventConfiguration {
    @Value("${disruptor.ring-buffer-size:1024}")
    private int disruptorRingBufferSize = 1024;
    @Setter
    @Value("${disruptor.debug:false}")
    private boolean debug;
    @Value("${disruptor.retry-count:1}")
    private int disruptorRetryCount;
    @Value("${disruptor.wait-strategy:0}")
    private int disruptorWaitStrategy;
    @Value("${spring.application.name:DisruptorEvent}")
    private String applicationName;
    @Value("${disruptor.name:DisruptorEvent}")
    private String disruptorName;
    @Value("${disruptor.max-available-processors:24}")
    private int maxAvailableProcessors = 24;
}
