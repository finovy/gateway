package tech.finovy.gateway.config.nacos.entity;

import java.io.Serializable;

public class NacosConfigDefaultItem implements Serializable {
    private String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
