package tech.finovy.gateway.config.nacos.listener;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.common.utils.MD5Utils;
import tech.finovy.gateway.config.nacos.entity.NacosCas;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class NacosConfigListener<T> implements Listener {
    public static final String JSON_TYPE = "json";
    public static final String TEXT_TYPE = "text";
    public static final String YAML_TYPE = "yaml";
    protected final AtomicInteger version;
    private final Map<String, NacosConfigDefinitionListener> nacosConfigDefinitionListener;
    private final Map<String, NacosCas> nacosConfigValue;


    public NacosConfigListener(Map<String, NacosCas> nacosConfigValue, Map<String, NacosConfigDefinitionListener> nacosConfigDefinitionListener, AtomicInteger version) {
        this.nacosConfigDefinitionListener = nacosConfigDefinitionListener;
        this.nacosConfigValue = nacosConfigValue;
        this.version = version;
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        NacosConfigDefinitionListener listener = null;
        int cnt = version.incrementAndGet();
        for (Map.Entry<String, NacosConfigDefinitionListener> item : nacosConfigDefinitionListener.entrySet()) {
            listener = item.getValue();
            listener.onReceive(configInfo, cnt);

        }
        if (listener != null && configInfo != null) {
            nacosConfigValue.put(listener.getType(), new NacosCas(configInfo, MD5Utils.md5Hex(configInfo, Constants.ENCODE)));
        }
    }
}
