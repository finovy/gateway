package tech.finovy.gateway.common.loader;


import org.apache.commons.lang3.StringUtils;

final class ExtensionDefinition<S> {

    private final String name;
    private final Class<S> serviceClass;
    private final Integer order;
    private final Scope scope;

    public ExtensionDefinition(String name, Integer order, Scope scope, Class<S> clazz) {
        this.name = name;
        this.order = order;
        this.scope = scope;
        this.serviceClass = clazz;
    }

    public Integer getOrder() {
        return this.order;
    }

    public Class<S> getServiceClass() {
        return this.serviceClass;
    }

    public Scope getScope() {
        return this.scope;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((serviceClass == null) ? 0 : serviceClass.hashCode());
        result = prime * result + ((order == null) ? 0 : order.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        ExtensionDefinition<?> other = (ExtensionDefinition<?>) obj;
        if (!StringUtils.equals(name, other.name)) {
            return false;
        }
        if (!serviceClass.equals(other.serviceClass)) {
            return false;
        }
        if (!order.equals(other.order)) {
            return false;
        }
        return !scope.equals(other.scope);
    }

    public String getName() {
        return name;
    }
}
