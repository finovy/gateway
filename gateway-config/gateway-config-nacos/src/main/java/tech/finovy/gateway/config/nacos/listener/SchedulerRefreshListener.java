package tech.finovy.gateway.config.nacos.listener;


import tech.finovy.gateway.common.chain.ChainListener;

public interface SchedulerRefreshListener extends ChainListener {
    void trigger(long trigger);

    void startup();
}
