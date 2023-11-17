package tech.finovy.gateway.manager.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.finovy.framework.config.nacos.listener.AbstractNacosConfigDefinitionListener;
import tech.finovy.gateway.context.ConfigurationContext;
import tech.finovy.gateway.context.ConfigurationContextHolder;
import tech.finovy.gateway.common.entity.HostGroupEntity;
import tech.finovy.gateway.common.entity.HostItemEntity;
import tech.finovy.gateway.router.RouteConfiguration;

import java.util.*;

@Component
@Slf4j
public class HostConfigDefinitionListener extends AbstractNacosConfigDefinitionListener<HostGroupEntity> {
    private static final Map<String, HostItemEntity> HOST_ITEM_ENTITY_MAP = new HashMap<>();
    ;
    private static final HostItemEntity HOST_ITEM_ENTITY = new HostItemEntity();
    @Autowired
    private RouteConfiguration routeConfiguration;
    private ConfigurationContext configurationContext = ConfigurationContextHolder.get();

    public HostConfigDefinitionListener(RouteConfiguration routeConfiguration) {
        super(HostGroupEntity.class, routeConfiguration.getHostDataId(), routeConfiguration.getRouteDataGroup());
        this.routeConfiguration = routeConfiguration;
        configurationContext.setHostItem(HOST_ITEM_ENTITY_MAP);
    }

    @Override
    public String getDataId() {
        return routeConfiguration.getHostDataId();
    }

    @Override
    public String getDataGroup() {
        return routeConfiguration.getRouteDataGroup();
    }

    @Override
    public void refresh(String dataId, String dataGroup, HostGroupEntity config, int version) {
        BeanUtils.copyProperties(config, HOST_ITEM_ENTITY);
        List<HostItemEntity> hostList = config.getHosts();
        if (hostList == null || hostList.isEmpty()) {
            return;
        }
        Set<String> tem = new HashSet<>(hostList.size());
        Set<String> exists = new HashSet<>(HOST_ITEM_ENTITY_MAP.size());
        for (HostItemEntity item : hostList) {
            tem.add(item.getHost());
            item.setExists(true);
            HOST_ITEM_ENTITY_MAP.put(item.getHost(), item);
        }
        exists.addAll(HOST_ITEM_ENTITY_MAP.keySet());
        for (String k : exists) {
            if (tem.contains(k)) {
                continue;
            }
            HOST_ITEM_ENTITY_MAP.remove(k);
        }
        configurationContext.setHostItem(HOST_ITEM_ENTITY_MAP);
    }

    @Override
    public int getOrder() {
        return super.getOrder() + 20;
    }
}
