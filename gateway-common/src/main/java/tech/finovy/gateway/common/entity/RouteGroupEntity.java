package tech.finovy.gateway.common.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class RouteGroupEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -1847899804045021045L;
    private List<RouteEntity> routeList;
    private List<GlobalRoutFilterEntity> globalFilters;
    private List<GlobalRoutePredicateEntity> globalpredicates;

}
