package tech.finovy.gateway.manager.listener;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.finovy.framework.config.nacos.listener.AbstractNacosConfigDefinitionListener;
import tech.finovy.gateway.context.ConfigurationContext;
import tech.finovy.gateway.context.ConfigurationContextHolder;
import tech.finovy.gateway.common.entity.SkipContentTypeItemEntity;
import tech.finovy.gateway.common.entity.SkipItemEntity;
import tech.finovy.gateway.common.entity.SkipUrlGroupEntity;
import tech.finovy.gateway.common.entity.SkipUrlItemEntity;
import tech.finovy.gateway.router.RouteConfiguration;

import java.util.*;

@Component
@Slf4j
public class UrlConfigDefinitionListener extends AbstractNacosConfigDefinitionListener<SkipUrlGroupEntity> {
    private static final Map<String, SkipUrlItemEntity> URL_ITEM_ENTITY_MAP = new LinkedHashMap<>();
    ;
    private static final Map<String, SkipContentTypeItemEntity> CONTENT_TYPE_ITEM_ENTITY_MAP = new LinkedHashMap<>();
    private static final Map<String, SkipUrlItemEntity> DEFAULT_URL_ITEM_ENTITY_MAP = new LinkedHashMap<>();
    private static final Map<String, SkipContentTypeItemEntity> DEFAULT_CONTENT_TYPE_ITEM_ENTITY_MAP = new LinkedHashMap<>();
    private static final Set<String> HEADERS_REMOVE_MAP = new HashSet<>();
    private static final SkipUrlItemEntity SKIP_URL_ITEM_ENTITY = new SkipUrlItemEntity();
    private static final String IMAGE = "image";
    private static final String FONT = "font";
    private static final String APPLICATION_JAVASCRIPT = "application/javascript";
    private static final String TEXT_CSS = "text/css";

    static {
        DEFAULT_URL_ITEM_ENTITY_MAP.put("ico", new SkipUrlItemEntity("ico", 10000, "/**/*.ico", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("js", new SkipUrlItemEntity("js", 10000, "/**/*.js", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("css", new SkipUrlItemEntity("css", 10000, "/**/*.css", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("jpeg", new SkipUrlItemEntity("jpeg", 10000, "/**/*.jpeg", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("png", new SkipUrlItemEntity("png", 10000, "/**/*.png", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("svg", new SkipUrlItemEntity("svg", 10000, "/**/*.svg", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("jpg", new SkipUrlItemEntity("jpg", 10000, "/**/*.jpg", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("gif", new SkipUrlItemEntity("gif", 10000, "/**/*.gif", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("ttf", new SkipUrlItemEntity("ttf", 10000, "/**/*.ttf", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("woff", new SkipUrlItemEntity("woff", 10000, "/**/*.woff", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("woff2", new SkipUrlItemEntity("woff2", 10000, "/**/*.woff2", true, true));
        DEFAULT_URL_ITEM_ENTITY_MAP.put("less", new SkipUrlItemEntity("less", 10000, "/**/*.less", true, true));

        CONTENT_TYPE_ITEM_ENTITY_MAP.put(IMAGE, new SkipContentTypeItemEntity(IMAGE, 10000, IMAGE, true, true));
        CONTENT_TYPE_ITEM_ENTITY_MAP.put(FONT, new SkipContentTypeItemEntity(FONT, 10000, FONT, true, true));
        CONTENT_TYPE_ITEM_ENTITY_MAP.put(APPLICATION_JAVASCRIPT, new SkipContentTypeItemEntity(APPLICATION_JAVASCRIPT, 10000, APPLICATION_JAVASCRIPT, true, true));
        CONTENT_TYPE_ITEM_ENTITY_MAP.put(TEXT_CSS, new SkipContentTypeItemEntity(TEXT_CSS, 10000, TEXT_CSS, true, true));
    }

    @Autowired
    private RouteConfiguration routeConfiguration;
    private ConfigurationContext configurationContext = ConfigurationContextHolder.get();

    public UrlConfigDefinitionListener(RouteConfiguration routeConfiguration) {
        super(SkipUrlGroupEntity.class, routeConfiguration.getSkipUrlDataId(), routeConfiguration.getRouteDataGroup());
        this.routeConfiguration = routeConfiguration;
        configurationContext.setSkipUrl(URL_ITEM_ENTITY_MAP);
        configurationContext.setSkipContentType(CONTENT_TYPE_ITEM_ENTITY_MAP);
        configurationContext.setDefaultSkipUrl(DEFAULT_URL_ITEM_ENTITY_MAP);
        configurationContext.setDefaultSkipContentType(DEFAULT_CONTENT_TYPE_ITEM_ENTITY_MAP);
        configurationContext.setRemoveHeaders(HEADERS_REMOVE_MAP);
    }

    public void addSkipUrlItemEntity(String key, SkipUrlItemEntity skipUrlItemEntity) {
        DEFAULT_URL_ITEM_ENTITY_MAP.put(key, skipUrlItemEntity);
    }

    public void addSkipContentTypeItemEntity(String key, SkipContentTypeItemEntity skipContentTypeItemEntity) {
        DEFAULT_CONTENT_TYPE_ITEM_ENTITY_MAP.put(key, skipContentTypeItemEntity);
    }

    @Override
    public String getDataId() {
        return routeConfiguration.getSkipUrlDataId();
    }

    @Override
    public String getDataGroup() {
        return routeConfiguration.getRouteDataGroup();
    }

    @Override
    public void refresh(String dataId, String dataGroup, SkipUrlGroupEntity config, int version) {
        log.info("Route Url data-id: {},data-group: {}", routeConfiguration.getSkipUrlDataId(), routeConfiguration.getRouteDataGroup());
        BeanUtils.copyProperties(config, SKIP_URL_ITEM_ENTITY);
        refreshData(CONTENT_TYPE_ITEM_ENTITY_MAP, config.getContentTypes(), "SkipContentType");
        refreshData(URL_ITEM_ENTITY_MAP, config.getSkips(), "URL");
        configurationContext.setSkipUrl(URL_ITEM_ENTITY_MAP);
        configurationContext.setSkipContentType(CONTENT_TYPE_ITEM_ENTITY_MAP);
        configurationContext.setDefaultSkipUrl(DEFAULT_URL_ITEM_ENTITY_MAP);
        configurationContext.setDefaultSkipContentType(DEFAULT_CONTENT_TYPE_ITEM_ENTITY_MAP);
        configurationContext.setRemoveHeaders(HEADERS_REMOVE_MAP);
        if(config.getHeaders()!=null){
            HEADERS_REMOVE_MAP.clear();
            int x = 1;
            for (String item : config.getHeaders()) {
                HEADERS_REMOVE_MAP.add(item);
                log.info("Refresh Header[{}/{}] Value:{}", x++, config.getHeaders().size(),item);
            }
        }
    }

    private <T extends SkipItemEntity> void refreshData(Map<String, T> skipItemEntityMap, List<T> itemEntities, String type) {
        if (itemEntities == null || itemEntities.isEmpty()) {
            return;
        }
        Set<String> tem = new HashSet<>(itemEntities.size());
        Set<String> exists = new HashSet<>(skipItemEntityMap.size());
        int x = 1;
        for (SkipItemEntity item : itemEntities) {
            item.setExists(true);
            tem.add(item.getKey());
            skipItemEntityMap.put(item.getKey(), (T) item);
            log.info("Refresh[{}/{}] Order:{}, {}:{},Value:{}", x++, itemEntities.size(), item.getOrder(), type, item.getKey(), JSON.toJSONString(item));
        }
        exists.addAll(skipItemEntityMap.keySet());
        for (String k : exists) {
            if (tem.contains(k)) {
                continue;
            }
            skipItemEntityMap.remove(k);
        }

    }

    @Override
    public int getOrder() {
        return super.getOrder() + 20;
    }
}
