package tech.finovy.gateway.manager.listener;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.finovy.framework.config.nacos.listener.AbstractNacosConfigDefinitionListener;
import tech.finovy.gateway.common.entity.RouteEntity;
import tech.finovy.gateway.common.entity.RouteGroupEntity;
import tech.finovy.gateway.router.FilterDefinitionExt;
import tech.finovy.gateway.router.PredicateDefinitionExt;
import tech.finovy.gateway.router.RouteConfiguration;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedMap;

@Component
@Slf4j
public class RouteConfigDefinitionListener extends AbstractNacosConfigDefinitionListener<RouteGroupEntity> implements RouteDefinitionRepository, ApplicationEventPublisherAware {

    private static final Map<String, RouteDefinition> NACOS_ROUTE_DEFINITION = synchronizedMap(new LinkedHashMap<>());
    @Autowired
    private RouteConfiguration routeConfiguration;
    private ApplicationEventPublisher applicationEventPublisher;

    public RouteConfigDefinitionListener(RouteConfiguration routeConfiguration) {
        super(RouteGroupEntity.class, routeConfiguration.getRouteDataId(), routeConfiguration.getRouteDataGroup());
        this.routeConfiguration = routeConfiguration;
        log.debug("Listener Route data-id: {},data-group: {}", routeConfiguration.getRouteDataId(), routeConfiguration.getRouteDataGroup());
    }

    @Override
    public String getDataId() {
        return routeConfiguration.getRouteDataId();
    }

    @Override
    public String getDataGroup() {
        return routeConfiguration.getRouteDataGroup();
    }

    @Override
    public void refresh(String dataId, String dataGroup, RouteGroupEntity config, int version) {
        assembleRouteDefinition(config);
    }

    private void assembleRouteDefinition(RouteGroupEntity config) {
        List<RouteEntity> routes = config.getRouteList();
        if (routes == null) {
            return;
        }
        routes.sort(Comparator.comparingInt(RouteEntity::getOrder));
        log.info("Load route config from nacos,DataID:{},DataGroup:{},size:{}", routeConfiguration.getRouteDataId(), routeConfiguration.getRouteDataGroup(), routes.size());
        Set<String> tem = new HashSet<>(routes.size());
        List<FilterDefinitionExt> globalFilters = Optional.ofNullable(config.getGlobalFilters()).orElse(new ArrayList<>()).stream().map(m -> {
            FilterDefinitionExt filterDefinition = new FilterDefinitionExt();
            filterDefinition.setArgs(m.getArgs());
            filterDefinition.setName(m.getName());
            filterDefinition.setApply(m.getApply());
            filterDefinition.setNotApply(m.getNotApply());
            return filterDefinition;
        }).collect(Collectors.toList());
        List<PredicateDefinitionExt> globalPredicates = Optional.ofNullable(config.getGlobalpredicates()).orElse(new ArrayList<>()).stream().map(m -> {
            PredicateDefinitionExt predicateDefinition = new PredicateDefinitionExt(m.toString());
            predicateDefinition.setName(m.getName());
            return predicateDefinition;
        }).collect(Collectors.toList());

        int x = 1;
        for (FilterDefinition routFilterEntity : globalFilters) {
            log.info("Load GlobalFilter[{}/{}],name={}", x++, globalFilters.size(), routFilterEntity.getName());
        }
        x = 1;
        for (PredicateDefinitionExt routFilterEntity : globalPredicates) {
            log.info("Load GlobalPredicate[{}/{}],name={}", x++, globalFilters.size(), routFilterEntity.getName());
        }
        x = 1;
        for (RouteEntity eachRoute : routes) {
            RouteDefinition definition = new RouteDefinition();
            definition.setId(eachRoute.getId());
            definition.setOrder(eachRoute.getOrder());
            List<PredicateDefinition> predicateDefinitionList = Optional.ofNullable(eachRoute.getPredicates()).orElse(new ArrayList<>()).stream().map(m -> {
                PredicateDefinition predicateDefinition = new PredicateDefinition(m.toString());
                predicateDefinition.setName(m.getName());
                return predicateDefinition;
            }).collect(Collectors.toList());
            List<PredicateDefinition> predicateGlobal = globalPredicates.stream().filter(f -> {
                if (f.isNotApply(definition.getId())) {
                    return false;
                }
                return f.isApply(definition.getId());
            }).collect(Collectors.toList());
            predicateDefinitionList.addAll(predicateGlobal);
            predicateDefinitionList = predicateDefinitionList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(PredicateDefinition::getName))), ArrayList::new));
            definition.setPredicates(predicateDefinitionList);
            List<FilterDefinition> filterDefinitionList = Optional.ofNullable(eachRoute.getFilters()).orElse(new ArrayList<>()).stream().map(m -> {
                FilterDefinition filterDefinition = new FilterDefinition();
                filterDefinition.setArgs(m.getArgs());
                filterDefinition.setName(m.getName());
                return filterDefinition;
            }).collect(Collectors.toList());
            List<FilterDefinition> filterGlobal = globalFilters.stream().filter(f -> {
                if (f.isNotApply(definition.getId())) {
                    return false;
                }
                return f.isApply(definition.getId());
            }).collect(Collectors.toList());
            filterDefinitionList.addAll(filterGlobal);
            filterDefinitionList = filterDefinitionList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(FilterDefinition::getName))), ArrayList::new));
            definition.setFilters(filterDefinitionList);
            if (eachRoute.getMetadata() != null) {
                definition.setMetadata(eachRoute.getMetadata());
            }
            URI uri = UriComponentsBuilder.fromUriString(eachRoute.getUri()).build().toUri();
            definition.setUri(uri);
            NACOS_ROUTE_DEFINITION.put(definition.getId(), definition);
            tem.add(eachRoute.getId());
            log.info("Refresh[{}/{}] Order:{},RoutID:{},URL:{}, Value:{}", x++, routes.size(), eachRoute.getOrder(), eachRoute.getId(), eachRoute.getUri(), JSON.toJSONString(eachRoute.getPredicates()));
        }
        Set<String> exists = new HashSet<>(NACOS_ROUTE_DEFINITION.size());
        exists.addAll(NACOS_ROUTE_DEFINITION.keySet());
        x = 0;
        for (String k : exists) {
            if (tem.contains(k)) {
                continue;
            }
            NACOS_ROUTE_DEFINITION.remove(k);
            log.info("Remove[{}] RoutID:{}", x, k);
        }
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            if (StringUtils.isEmpty(r.getId())) {
                return Mono.error(new IllegalArgumentException("id may not be empty"));
            }
            NACOS_ROUTE_DEFINITION.put(r.getId(), r);
            applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (NACOS_ROUTE_DEFINITION.containsKey(id)) {
                NACOS_ROUTE_DEFINITION.remove(id);
                applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
                return Mono.empty();
            }
            return Mono.defer(() -> Mono.error(
                    new NotFoundException("RouteDefinition not found: " + routeId)));
        });
    }

    @Override
    public int getOrder() {
        return super.getOrder() + 20;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(NACOS_ROUTE_DEFINITION.values());
    }
}
