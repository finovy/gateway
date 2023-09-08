package tech.finovy.gateway.common.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Data
public class RoutePredicateEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -1867899804045021045L;
    private static final String SYMBOL = ",";
    private static final String[] CONTAIN = new String[]{"After", "Before", "Between", "Cookie", "Header", "Host", "Method", "Path", "Query", "RemoteAddr"};
    private String name;
    private Set<String> values = new HashSet<>();

    public void setName(String name) {
        if (Arrays.asList(CONTAIN).contains(name)) {
            this.name = name;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(name);
        sb.append("=");
        for (String u : values) {
            sb.append(u).append(SYMBOL);
        }
        if (sb.lastIndexOf(SYMBOL) > 0) {
            sb.deleteCharAt(sb.lastIndexOf(SYMBOL));
        }
        return sb.toString();
    }
}
