package tech.finovy.gateway.config.nacos.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

@Slf4j
public abstract class AbstractNacosConfigDefinitionListener<T> implements NacosConfigDefinitionListener<T> {

    private final Class<T> dataType;
    protected String dataId;
    protected String dataGroup;
    protected String namespace;
    protected String index;
    protected AbstractNacosConfigDefinitionListener(Class<T> dataType, String dataId, String dataGroup) {
        this.dataId = dataId;
        this.dataGroup = dataGroup;
        this.dataType = dataType;
    }

    protected AbstractNacosConfigDefinitionListener(Class<T> dataType, String dataId, String dataGroup, String namespace) {
        this.dataId = dataId;
        this.dataGroup = dataGroup;
        this.dataType = dataType;
        this.namespace = namespace;
    }

    public AbstractNacosConfigDefinitionListener(Class<T> dataType) {
        this.dataType = dataType;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public long getTimeout() {
        return 2500L;
    }

    @Override
    public String getKey() {
        return String.join("_", this.getClass().getSimpleName(), getNameSpace(), getDataGroup(), getDataId());
    }

    @Override
    public String getType() {
        return String.join("_", getNameSpace(), getDataGroup(), getDataId());
    }

    @Override
    public String getNameSpace() {
        return namespace == null ? "DEFAULT_NAMESPACE" : namespace;
    }


    @Override
    public T parseObject(String config, int version) {
        if (StringUtils.isBlank(config)) {
            log.debug("Nacos Config not exists data-id: {},data-group: {},namespace:{}, Load Count：{}", getDataId(), getDataGroup(), getNameSpace(), version);
            onError(getDataId(), getDataGroup(), null, "Config not exists");
            return null;
        }
        if (StringUtils.endsWith(getDataId(), NacosConfigListener.TEXT_TYPE)) {
            log.debug("TXT data-id: {},data-group: {},namespace:{}, Load Count：{}", getDataId(), getDataGroup(), getNameSpace(), version);
            return (T) config;
        }
        if (StringUtils.endsWith(getDataId(), NacosConfigListener.JSON_TYPE)) {
            log.debug("JSON data-id: {},data-group: {},namespace:{}, Load Count：{}", getDataId(), getDataGroup(), getNameSpace(), version);
            return parseJson(config);
        }
        if (StringUtils.endsWith(getDataId(), NacosConfigListener.YAML_TYPE)) {
            log.debug("YAML data-id: {},data-group: {},namespace:{}, Load Count：{}", getDataId(), getDataGroup(), getNameSpace(), version);
            return parseYaml(config);
        }
        log.debug("JSON data-id: {},data-group: {} ,namespace:{}, Load Count:{}", getDataId(), getDataGroup(), getNameSpace(), version);
        return parseJson(config);
    }

    @Override
    public void onReceive(String config, int version) {
        if (config == null) {
            log.warn("Receive NULL data-id: {},data-group: {},namespace:{}, Load Count：{}", getDataId(), getDataGroup(), getNameSpace(), version);
            return;
        }
        log.debug("onReceive data-id: {},data-group: {},namespace:{}, Load Count：{}", getDataId(), getDataGroup(), getNameSpace(), version);
        T t = parseObject(config, version);
        if (t != null) {
            try {
                refresh(getDataId(), getDataGroup(), t, version);
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
    }

    @Override
    public void onError(String dataId, String dataGroup, String config, String errMsg) {
        log.warn("Nacos Listener WARN, data-id: {},data-group: {},namespace:{},msessage:{},config:{}", dataId, dataGroup, getNameSpace(), errMsg, config);
    }


    private T parseJson(String config) {
        JSONValidator validator = JSONValidator.from(config);
        if (!validator.validate()) {
            log.error("Nacos JSONValidator ERROR,DataID:{},DataGroup:{},namespace:{}, Load Count:{}", getDataId(), getDataGroup(), getNameSpace());
            return null;
        }
        try {
            JSONObject json = JSON.parseObject(config);
            return json.toJavaObject(dataType);
        } catch (Exception e) {
            log.error("ParseJson to {},ERROR:{}", dataType, e.toString());
            onError(getDataId(), getDataGroup(), config, e.toString());
        }
        return null;
    }

    private T parseYaml(String config) {
        try {
            Yaml yaml = new Yaml();
            return yaml.loadAs(config, dataType);
        } catch (Exception e) {
            log.error("ParseYaml to {},ERROR:{}", dataType, e.toString());
            onError(getDataId(), getDataGroup(), config, e.toString());
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getIndex() {
        return index;
    }

    @Override
    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void init() {

    }
}
