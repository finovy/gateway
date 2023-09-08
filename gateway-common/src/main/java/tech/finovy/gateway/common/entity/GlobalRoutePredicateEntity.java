package tech.finovy.gateway.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class GlobalRoutePredicateEntity extends RoutePredicateEntity implements Serializable {
    private Set<String> apply;
    private Set<String> notApply;
}
