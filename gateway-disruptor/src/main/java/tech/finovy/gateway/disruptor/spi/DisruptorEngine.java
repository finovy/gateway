package tech.finovy.gateway.disruptor.spi;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import tech.finovy.gateway.disruptor.core.event.DisruptorEvent;

import java.util.List;

public interface DisruptorEngine {
    void post(DisruptorEvent event);

    void addProcessInterfaces(List<ProcessInterface> processInterfaces);

    void addProcessInterface(ProcessInterface processInterfaces);

    void start();

    void shutdown();

    default WaitStrategy switchWaitStrategy(int type) {
        switch (type) {
            case 1:
                return new SleepingWaitStrategy();
            case 2:
                return new YieldingWaitStrategy();
            default:
                return new BlockingWaitStrategy();
        }
    }
}
