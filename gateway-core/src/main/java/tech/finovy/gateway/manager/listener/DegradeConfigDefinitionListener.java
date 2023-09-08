package tech.finovy.gateway.manager.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.finovy.gateway.common.configuration.ExceptionConfiguration;
import tech.finovy.gateway.common.context.ConfigurationContext;
import tech.finovy.gateway.common.context.ConfigurationContextHolder;
import tech.finovy.gateway.common.entity.DegradeEntity;
import tech.finovy.gateway.common.entity.DegradeGroupEntity;
import tech.finovy.gateway.config.nacos.listener.AbstractNacosConfigDefinitionListener;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedMap;

@Component
@Slf4j
public class DegradeConfigDefinitionListener extends AbstractNacosConfigDefinitionListener<DegradeGroupEntity> {

    private static final DegradeEntity DEGRADE_GROUP_ENTITY = new DegradeEntity();
    private static final Map<String, DegradeEntity> NACOS_DEGRADE_DEFINITION = synchronizedMap(new LinkedHashMap<>());
    @Autowired
    private ExceptionConfiguration exceptionConfiguration;
    private ConfigurationContext configurationContext = ConfigurationContextHolder.get();

    public DegradeConfigDefinitionListener(ExceptionConfiguration exceptionConfiguration) {
        super(DegradeGroupEntity.class, exceptionConfiguration.getDegradeDataId(), exceptionConfiguration.getDegradeDataGroup());
        this.exceptionConfiguration = exceptionConfiguration;
        log.debug("Listener Route data-id: {},data-group: {}", exceptionConfiguration.getDegradeDataId(), exceptionConfiguration.getDegradeDataGroup());
        configurationContext.setDegrade(NACOS_DEGRADE_DEFINITION);
        configurationContext.setDefaultDegrade(DEGRADE_GROUP_ENTITY);
    }

    @Override
    public String getDataId() {
        return exceptionConfiguration.getDegradeDataId();
    }

    @Override
    public String getDataGroup() {
        return exceptionConfiguration.getDegradeDataGroup();
    }

    @Override
    public void refresh(String dataId, String dataGroup, DegradeGroupEntity config, int version) {
        DEGRADE_GROUP_ENTITY.setDegradeCode(exceptionConfiguration.getDegradeCode());
        DEGRADE_GROUP_ENTITY.setDegradeMessage(exceptionConfiguration.getDegradeMessage());
        DEGRADE_GROUP_ENTITY.setParamFlowCode(exceptionConfiguration.getDegradeCode());
        DEGRADE_GROUP_ENTITY.setParamFlowMessage(exceptionConfiguration.getDegradeMessage());
        DEGRADE_GROUP_ENTITY.setSystemBlockCode(exceptionConfiguration.getDegradeCode());
        DEGRADE_GROUP_ENTITY.setSystemBlockMessage(exceptionConfiguration.getDegradeMessage());
        DEGRADE_GROUP_ENTITY.setRouteId("DEFAULT");
        configurationContext.setDefaultDegrade(DEGRADE_GROUP_ENTITY);
        assembleDegradeDefinition(config);
    }

    private void assembleDegradeDefinition(DegradeGroupEntity config) {
        if (config.getDegradeCode() == 0) {
            config.setDegradeCode(exceptionConfiguration.getDegradeCode());
        }
        if (StringUtils.isBlank(config.getDegradeMessage())) {
            config.setDegradeMessage(exceptionConfiguration.getDegradeMessage());
        }
        if (StringUtils.isBlank(config.getParamFlowMessage())) {
            config.setParamFlowMessage(exceptionConfiguration.getDegradeMessage());
        }
        if (config.getParamFlowCode() == 0) {
            config.setParamFlowCode(exceptionConfiguration.getDegradeCode());
        }
        if (StringUtils.isBlank(config.getSystemBlockMessage())) {
            config.setSystemBlockMessage(exceptionConfiguration.getDegradeMessage());
        }
        if (config.getSystemBlockCode() == 0) {
            config.setSystemBlockCode(exceptionConfiguration.getDegradeCode());
        }
        BeanUtils.copyProperties(config, DEGRADE_GROUP_ENTITY);
        DEGRADE_GROUP_ENTITY.setRouteId("DEFAULT");
        List<DegradeEntity> routeConfig = Optional.ofNullable(config.getConfig()).orElse(new ArrayList<>()).stream().peek(m -> {
            if (StringUtils.isBlank(m.getDegradeMessage())) {
                m.setDegradeMessage(config.getDegradeMessage());
            }
            if (m.getDegradeCode() == 0) {
                m.setDegradeCode(config.getDegradeCode());
            }
            if (StringUtils.isBlank(m.getParamFlowMessage())) {
                m.setParamFlowMessage(config.getParamFlowMessage());
            }
            if (m.getParamFlowCode() == 0) {
                m.setParamFlowCode(config.getParamFlowCode());
            }
            if (StringUtils.isBlank(m.getSystemBlockMessage())) {
                m.setSystemBlockMessage(config.getSystemBlockMessage());
            }
            if (m.getSystemBlockCode() == 0) {
                m.setSystemBlockCode(config.getSystemBlockCode());
            }
            if (StringUtils.isBlank(m.getSystemBlockApi())) {
                m.setSystemBlockApi(config.getSystemBlockApi());
            }
            if (StringUtils.isBlank(m.getParamFlowApi())) {
                m.setParamFlowApi(config.getParamFlowApi());
            }
            if (StringUtils.isBlank(m.getDegradeApi())) {
                m.setDegradeApi(config.getDegradeApi());
            }
            NACOS_DEGRADE_DEFINITION.put(m.getRouteId(), m);
            log.info("Init Degrade:{},DegradeCode:{},DegradeMessage:{},ParamFlowCode:{},ParamFlowMessage:{},SystemBlockCode:{},SystemBlockMessage:{},DegradeApi:{},ParamFlowApi:{},SystemBlockApi:{}",
                    m.getRouteId(), m.getDegradeCode(), m.getDegradeMessage(), m.getParamFlowCode(), m.getParamFlowMessage(), m.getSystemBlockCode(), m.getSystemBlockMessage(), m.getDegradeApi(), m.getParamFlowApi(), m.getSystemBlockApi());
        }).collect(Collectors.toList());
        log.info("Init Degrade size:{}", routeConfig.size());
    }
}
