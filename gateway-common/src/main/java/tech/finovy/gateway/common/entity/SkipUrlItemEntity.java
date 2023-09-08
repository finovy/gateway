package tech.finovy.gateway.common.entity;

import lombok.Data;

@Data
public class SkipUrlItemEntity extends SkipItemEntity {
    private String url;
    private boolean directPass;
    private boolean skipLog;
    private boolean skipRefreshListener;
    private boolean skipAuth;
    private boolean skipResponse;
    private boolean skipRequest;
    private boolean skipEncry;
    private boolean skipDecry;
    private String decryType = "AES";

    public SkipUrlItemEntity(String key, String url) {
        this.key = key;
        this.url = url;
    }

    public SkipUrlItemEntity(String key, int order, String url, boolean skipLog, boolean skipAuth) {
        this.key = key;
        this.order = order;
        this.url = url;
        this.skipLog = skipLog;
        this.skipAuth = skipAuth;
    }

    public SkipUrlItemEntity() {
    }

    public String getDecryType() {
        if (this.skipDecry) {
            return "NONE";
        }
        return this.decryType;
    }

    @Override
    public String getKey() {
        if (key == null) {
            key = url;
        }
        return key;
    }
}
