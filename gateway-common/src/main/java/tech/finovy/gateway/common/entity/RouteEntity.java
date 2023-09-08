package tech.finovy.gateway.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class RouteEntity implements Serializable {
    private static final long serialVersionUID = -1857899804045021045L;
    private String id;
    private List<RoutePredicateEntity> predicates = new ArrayList<>();
    private List<RoutFilterEntity> filters;
    private Map<String, Object> metadata;
    private String uri;
    private int order = 0;

}
