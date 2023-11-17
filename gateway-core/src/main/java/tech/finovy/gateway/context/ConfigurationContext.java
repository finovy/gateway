package tech.finovy.gateway.context;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import tech.finovy.gateway.common.entity.DegradeEntity;
import tech.finovy.gateway.common.entity.HostItemEntity;
import tech.finovy.gateway.common.entity.SkipContentTypeItemEntity;
import tech.finovy.gateway.common.entity.SkipUrlItemEntity;

import java.util.*;

@Getter
@Setter
public class ConfigurationContext {
    private static final SkipUrlItemEntity SKIP_URL_ITEM_ENTITY = new SkipUrlItemEntity();
    private static final HostItemEntity HOST_ITEM_ENTITY = new HostItemEntity();
    private final PathMatcher matcher = new AntPathMatcher();
    private Map<String, SkipUrlItemEntity> skipUrl;
    private Map<String, SkipUrlItemEntity> defaultSkipUrl;
    private Map<String, SkipContentTypeItemEntity> skipContentType;
    private Map<String, SkipContentTypeItemEntity> defaultSkipContentType;
    private Map<String, HostItemEntity> hostItem;
    private Set<String> removeHeaders;
    private DegradeEntity defaultDegrade;
    private Map<String, DegradeEntity> degrade;

    public SkipUrlItemEntity pathContain(String path) {
        if (CollectionUtils.isEmpty(skipUrl) && CollectionUtils.isEmpty(defaultSkipUrl)) {
            return SKIP_URL_ITEM_ENTITY;
        }

        List<SkipUrlItemEntity> urlItems = new ArrayList<>();
        for (Map.Entry<String, SkipUrlItemEntity> pattern : skipUrl.entrySet()) {
            SkipUrlItemEntity entity = pattern.getValue();
            if (entity != null && matcher.match(entity.getUrl(), path)) {
                urlItems.add(entity);
            }
        }
        if (!CollectionUtils.isEmpty(urlItems)) {
            // 优先取完全匹配的路由
            SkipUrlItemEntity entity = urlItems.stream().filter(item -> path.equals(item.getKey())).findAny().orElse(null);
            if (Objects.nonNull(entity)) {
                entity.setExists(true);
                return entity;
            }
            return urlItems.get(0);
        }

        for (Map.Entry<String, SkipUrlItemEntity> pattern : defaultSkipUrl.entrySet()) {
            SkipUrlItemEntity entity = pattern.getValue();
            if (entity != null && matcher.match(entity.getUrl(), path)) {
                urlItems.add(entity);
            }
        }
        if (!CollectionUtils.isEmpty(urlItems)) {
            // 优先取完全匹配的路由
            SkipUrlItemEntity entity = urlItems.stream().filter(item -> path.equals(item.getKey())).findAny().orElse(null);
            if (Objects.nonNull(entity)) {
                entity.setExists(true);
                return entity;
            }
            return urlItems.get(0);
        }

        return SKIP_URL_ITEM_ENTITY;
    }

    public SkipContentTypeItemEntity contentTypeContain(String contentType) {
        if (CollectionUtils.isEmpty(skipContentType)) {
            for (Map.Entry<String, SkipContentTypeItemEntity> pattern : defaultSkipContentType.entrySet()) {
                SkipContentTypeItemEntity entity = pattern.getValue();
                if (entity != null && StringUtils.containsIgnoreCase(contentType, entity.getContentType())) {
                    entity.setExists(true);
                    return entity;
                }
            }
        }
        for (Map.Entry<String, SkipContentTypeItemEntity> pattern : skipContentType.entrySet()) {
            SkipContentTypeItemEntity entity = pattern.getValue();
            if (entity != null && StringUtils.containsIgnoreCase(contentType, entity.getContentType())) {
                entity.setExists(true);
                return entity;
            }
        }

        return new SkipContentTypeItemEntity(contentType, contentType);
    }

    public HostItemEntity getHostItemEntity(String host) {
        return hostItem.getOrDefault(host, HOST_ITEM_ENTITY);
    }

    public DegradeEntity getDegradeEntity(String routeId) {
        return degrade.getOrDefault(routeId, defaultDegrade);
    }
}
