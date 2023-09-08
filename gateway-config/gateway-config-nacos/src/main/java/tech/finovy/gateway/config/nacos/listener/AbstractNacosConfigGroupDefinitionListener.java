package tech.finovy.gateway.config.nacos.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.yaml.snakeyaml.Yaml;
import tech.finovy.gateway.config.nacos.entity.AbstractNacosConfigEntity;
import tech.finovy.gateway.config.nacos.entity.AbstractNacosConfigGroup;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.synchronizedMap;

@Slf4j
public abstract class AbstractNacosConfigGroupDefinitionListener<T extends AbstractNacosConfigGroup, E extends AbstractNacosConfigEntity> implements NacosConfigDefinitionListener<T> {
    protected final AtomicInteger loadNacos = new AtomicInteger();
    protected final Map<String, E> ABSTRACT_NACOS_DEFINITION_REPOSITORY_MAP = synchronizedMap(new LinkedHashMap<>());
    protected final E NACOS_DEFINITION_REPOSITORY_ITEM_ENTITY;
    private final Class<T> groupType;
    protected String dataId;
    protected String dataGroup;
    protected String namespace;
    protected String index;
    protected T confgiGroup;

    public AbstractNacosConfigGroupDefinitionListener(Class<T> groupType, Class<E> entityType, E defaultEntity) {
        this.groupType = groupType;
        NACOS_DEFINITION_REPOSITORY_ITEM_ENTITY = defaultEntity;
    }

    public AbstractNacosConfigGroupDefinitionListener(Class<T> groupType, Class<E> entityType, E defaultEntity, String dataId, String dataGroup) {
        this.dataId = dataId;
        this.dataGroup = dataGroup;
        this.groupType = groupType;
        NACOS_DEFINITION_REPOSITORY_ITEM_ENTITY = defaultEntity;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    public Map<String, E> getNacosDefinitionRepository() {
        return ABSTRACT_NACOS_DEFINITION_REPOSITORY_MAP;
    }

    @Override
    public long getTimeout() {
        return 1500L;
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
    public T parseObject(String config, int refreshCount) {
        long cnt = loadNacos.get();
        if (StringUtils.isBlank(config)) {
            log.debug("Nacos Config not exists data-id: {},data-group: {},namespace:{}, Load Count：{}", getDataId(), getDataGroup(), getNameSpace(), cnt);
            onError(getDataId(), getDataGroup(), null, "Config not exists");
            return null;
        }
        if (StringUtils.endsWith(dataId, NacosConfigListener.TEXT_TYPE)) {
            log.warn("TXT data-id: {},data-group: {},namespace:{}, Load Count：{}", getDataId(), getDataGroup(), getNameSpace(), cnt);
        }
        if (StringUtils.endsWith(getDataId(), NacosConfigListener.JSON_TYPE)) {
            log.debug("JSON data-id: {},data-group: {},namespace:{}, Load Count：{}", getDataId(), getDataGroup(), getNameSpace(), cnt);
            return parseJson(config);
        }
        if (StringUtils.endsWith(getDataId(), NacosConfigListener.YAML_TYPE)) {
            log.debug("YAML data-id: {},data-group: {},namespace:{}, Load Count：{}", getDataId(), getDataGroup(), getNameSpace(), cnt);
            return parseYaml(config);
        }
        log.debug("JSON data-id: {},data-group: {} ,namespace:{}, Load Count:{}", getDataId(), getDataGroup(), getNameSpace(), cnt);
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

    @Override
    public void refresh(String dataId, String dataGroup, T config, int version) {
        BeanUtils.copyProperties(config, NACOS_DEFINITION_REPOSITORY_ITEM_ENTITY);
        if (confgiGroup == null) {
            confgiGroup = config;
        } else {
            BeanUtils.copyProperties(config, confgiGroup);
        }
        NACOS_DEFINITION_REPOSITORY_ITEM_ENTITY.setExists(false);
        List<E> hostList = config.getEntity();
        if (hostList == null || hostList.isEmpty()) {
            return;
        }
        Set<String> tem = new HashSet<>(hostList.size());
        Set<String> exists = new HashSet<>(ABSTRACT_NACOS_DEFINITION_REPOSITORY_MAP.size());
        for (E item : hostList) {
            item.setExists(true);
            tem.add(item.getKey());
            E existsItem = ABSTRACT_NACOS_DEFINITION_REPOSITORY_MAP.getOrDefault(item.getKey(), item);
            BeanUtils.copyProperties(item, existsItem);
            ABSTRACT_NACOS_DEFINITION_REPOSITORY_MAP.put(item.getKey(), item);
        }
        exists.addAll(ABSTRACT_NACOS_DEFINITION_REPOSITORY_MAP.keySet());
        for (String k : exists) {
            if (tem.contains(k)) {
                continue;
            }
            ABSTRACT_NACOS_DEFINITION_REPOSITORY_MAP.remove(k);
        }
        log.debug("Listener AbstractNacosGroup refersh finished data-id: {},data-group: {}", dataId, dataGroup);
    }

    private T parseJson(String config) {
        JSONValidator validator = JSONValidator.from(config);
        if (!validator.validate()) {
            log.error("Nacos JSONValidator ERROR,DataID:{},DataGroup:{},namespace:{}, Load Count:{}", getDataId(), getDataGroup(), getNameSpace());
            return null;
        }
        try {
            JSONObject json = JSON.parseObject(config);
            return json.toJavaObject(groupType);
        } catch (Exception e) {
            log.error("ParseJson to {},ERROR:{}", groupType, e.toString());
            onError(getDataId(), getDataGroup(), config, e.toString());
        }
        return null;
    }

    private T parseYaml(String config) {
        try {
            Yaml yaml = new Yaml();
            return yaml.loadAs(config, groupType);
        } catch (Exception e) {
            log.error("ParseYaml to {},ERROR:{}", groupType, e.toString());
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


    public E getEntity(String key) {
        E entity = Optional.ofNullable(ABSTRACT_NACOS_DEFINITION_REPOSITORY_MAP.get(key)).orElse(NACOS_DEFINITION_REPOSITORY_ITEM_ENTITY);
        if (!entity.isExists()) {
            log.warn("AbstractNacosEntity:{} is NOT EXISTS", key);
        }
        return entity;
    }

    public T getConfgiGroup() {
        return confgiGroup;
    }

    public E getDefaultEntity() {
        return NACOS_DEFINITION_REPOSITORY_ITEM_ENTITY;
    }

}
