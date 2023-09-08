package tech.finovy.gateway.config.nacos.context;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import tech.finovy.gateway.common.chain.ChainSortUtil;
import tech.finovy.gateway.config.nacos.entity.NacosCas;
import tech.finovy.gateway.config.nacos.listener.NacosConfigDefinitionListener;
import tech.finovy.gateway.config.nacos.listener.NacosConfigListener;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NacosContext {
    private final static Set<String> EXISTS_NACOS_CONFIG_DEFINITION_LISTENER = new HashSet<>();
    protected final AtomicInteger refreshCount = new AtomicInteger();
    private final Map<String, Map<String, NacosConfigDefinitionListener>> NACOS_LISTENER_CONCURRENT_HASH_MAP = new LinkedHashMap<>();
    private final Map<String, NacosCas> NACOS_CONFIG_VALUE_HASH_MAP = new LinkedHashMap<>();
    private ConfigService configService;


    public NacosContext(ConfigService configService) {
        this.configService = configService;
    }

    public NacosContext(ConfigService configService, List<NacosConfigDefinitionListener> listeners) {
        this.configService = configService;
        NACOS_LISTENER_CONCURRENT_HASH_MAP.putAll(ChainSortUtil.multiChainListenerSort(listeners));
        init();
    }

    public void init() {

        for (Map.Entry<String, Map<String, NacosConfigDefinitionListener>> endpoint : NACOS_LISTENER_CONCURRENT_HASH_MAP.entrySet()) {
            try {
                Map<String, NacosConfigDefinitionListener> action = endpoint.getValue();
                if (action.isEmpty()) {
                    continue;
                }
                String key = endpoint.getKey();
                if (EXISTS_NACOS_CONFIG_DEFINITION_LISTENER.contains(key)) {
                    log.debug("Skip Exists NacosConfigDefinitionListener:{}", key);
                    continue;
                }
                NacosConfigListener nacosConfigListener = new NacosConfigListener(NACOS_CONFIG_VALUE_HASH_MAP, action, refreshCount);
                for (NacosConfigDefinitionListener listener : action.values()) {
                    String configInfo = configService.getConfigAndSignListener(listener.getDataId(), listener.getDataGroup(), listener.getTimeout(), nacosConfigListener);
                    if (StringUtils.isBlank(configInfo)) {
                        configInfo = configService.getConfig(listener.getDataId(), listener.getDataGroup(), listener.getTimeout());
                    }
                    EXISTS_NACOS_CONFIG_DEFINITION_LISTENER.add(key);
                    log.info("Registry NacosConfigDefinitionListener:{} ,DataId:{},DataGroup:{},Timeout:{}", key, listener.getDataId(), listener.getDataGroup(), listener.getTimeout());
                    if (StringUtils.isNotBlank(configInfo)) {
                        NACOS_CONFIG_VALUE_HASH_MAP.put(key, new NacosCas(configInfo, MD5Utils.md5Hex(configInfo, Constants.ENCODE)));
                    }
                    if (EXISTS_NACOS_CONFIG_DEFINITION_LISTENER.contains(key)) {
                        break;
                    }
                }
            } catch (NacosException e) {
                log.error("Init NacosConfigDefinitionListener error:{}", e.toString());
            }
        }
        log.info("Init all NacosConfigListener success------------------------");
        for (Map.Entry<String, NacosCas> item : NACOS_CONFIG_VALUE_HASH_MAP.entrySet()) {
            try {
                String key = item.getKey();
                Map<String, NacosConfigDefinitionListener> map = NACOS_LISTENER_CONCURRENT_HASH_MAP.get(key);
                int cnt = refreshCount.incrementAndGet();
                for (Map.Entry<String, NacosConfigDefinitionListener> hadler : map.entrySet()) {
                    NacosConfigDefinitionListener nacosConfigDefinitionListener = hadler.getValue();
                    log.info("Refresh NacosConfigDefinition:{},Index:{}", item.getKey(), nacosConfigDefinitionListener.getIndex());
                    nacosConfigDefinitionListener.onReceive(item.getValue().getContext(), cnt);
                }
            } catch (Exception e) {
                log.error("OnReceive NacosConfigDefinitionListener error:{}", e.toString());
                throw new RuntimeException(e);
            }
        }
    }

    public void addNacosConfigDefinitionListeners(List<NacosConfigDefinitionListener> listeners) {
        Map<String, Map<String, NacosConfigDefinitionListener>> sorListener = ChainSortUtil.multiChainListenerSort(listeners);
        for (Map.Entry<String, Map<String, NacosConfigDefinitionListener>> item : sorListener.entrySet()) {
            Map<String, NacosConfigDefinitionListener> ex = NACOS_LISTENER_CONCURRENT_HASH_MAP.get(item.getKey());
            if (ex == null) {
                NACOS_LISTENER_CONCURRENT_HASH_MAP.put(item.getKey(), item.getValue());
            } else {
                ex.putAll(item.getValue());
                Map<String, NacosConfigDefinitionListener> m = ChainSortUtil.singleChainListenerSort(ex);
                ex.clear();
                ex.putAll(m);
            }
        }
        init();
    }

    public <T> T getNacosConfigDefinition(NacosConfigDefinitionListener<T> listener) {
        if (NACOS_CONFIG_VALUE_HASH_MAP.containsKey(listener.getType())) {
            return listener.parseObject(NACOS_CONFIG_VALUE_HASH_MAP.get(listener.getType()).getContext(), refreshCount.incrementAndGet());
        }
        Map<String, NacosConfigDefinitionListener> type = NACOS_LISTENER_CONCURRENT_HASH_MAP.get(listener.getType());
        String key = listener.getKey();
        if (type == null || !type.containsKey(key)) {
            List<NacosConfigDefinitionListener> configurationEndpoints = new ArrayList<>();
            configurationEndpoints.add(listener);
            addNacosConfigDefinitionListeners(configurationEndpoints);
        }
        if (NACOS_CONFIG_VALUE_HASH_MAP.containsKey(listener.getType())) {
            return listener.parseObject(NACOS_CONFIG_VALUE_HASH_MAP.get(listener.getType()).getContext(), refreshCount.incrementAndGet());
        }
        return null;
    }

    public <T> boolean publish(T config, String dataId, String dataGroup, String nameSpace) throws NacosException {
        if (config == null) {
            log.error("Input config is null");
            return false;
        }
        NacosCas nacosCas = NACOS_CONFIG_VALUE_HASH_MAP.get(String.join("_", nameSpace, dataGroup, dataId));
        String casMd5 = nacosCas == null ? null : nacosCas.getCasMd5();
        log.debug("Publish data-id: {},data-group: {}", dataId, dataGroup);
        if (StringUtils.endsWith(dataId, NacosConfigListener.JSON_TYPE)) {
            return configService.publishConfigCas(dataId, dataGroup, JSON.toJSONString(config, true), casMd5, ConfigType.JSON.getType());
        }
        if (StringUtils.endsWith(dataId, NacosConfigListener.YAML_TYPE)) {
            Yaml yaml = new Yaml();
            return configService.publishConfigCas(dataId, dataGroup, yaml.dump(config), casMd5, ConfigType.YAML.getType());
        }
        if (StringUtils.endsWith(dataId, NacosConfigListener.TEXT_TYPE) || config instanceof String) {
            return configService.publishConfigCas(dataId, dataGroup, (String) config, casMd5, ConfigType.TEXT.getType());
        }
        if (config instanceof String) {
            return configService.publishConfigCas(dataId, dataGroup, (String) config, casMd5, ConfigType.TEXT.getType());
        }
        return configService.publishConfigCas(dataId, dataGroup, JSON.toJSONString(config, true), casMd5, ConfigType.JSON.getType());
    }

    boolean removeConfig(String dataId, String group) throws NacosException {
        return configService.removeConfig(dataId, group);
    }
}
