package tech.finovy.gateway.common.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


@Data
public class RoutFilterEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -1877899804045021045L;
    private String name;
    private Map<String, String> args = new LinkedHashMap<>();
}
