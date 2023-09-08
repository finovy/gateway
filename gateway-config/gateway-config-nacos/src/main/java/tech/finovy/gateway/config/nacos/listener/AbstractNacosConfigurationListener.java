package tech.finovy.gateway.config.nacos.listener;

import com.alibaba.nacos.api.config.listener.Listener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class AbstractNacosConfigurationListener implements Listener {
    @Override
    public Executor getExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
