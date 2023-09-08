package tech.finovy.gateway.config.nacos.listener;


import tech.finovy.gateway.common.chain.ChainListener;

public interface NacosConfigDefinitionListener<T> extends ChainListener {
    String getDataId();

    String getDataGroup();

    String getNameSpace();

    long getTimeout();

    <T> T parseObject(String config, int version);

    void onReceive(String config, int version);

    void onError(String dataId, String dataGroup, String config, String errMsg);

    void refresh(String dataId, String dataGroup, T config, int version);
}
