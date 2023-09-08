package tech.finovy.gateway.config.nacos.entity;

public abstract class AbstractNacosConfigEntity extends NacosConfigDefaultItem {
    private String key;
    private boolean exists;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
