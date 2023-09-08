package tech.finovy.gateway.common.entity;

import lombok.Data;

@Data
public class SkipContentTypeItemEntity extends SkipItemEntity {
    private String contentType;
    private boolean skipLog;
    private boolean skipResponse;

    public SkipContentTypeItemEntity(String key, String contentType) {
        this.key = key;
        this.contentType = contentType;
    }

    public SkipContentTypeItemEntity(String key, int order, String contentType, boolean skipLog, boolean skipResponse) {
        this.key = key;
        this.order = order;
        this.contentType = contentType;
        this.skipLog = skipLog;
        this.skipResponse = skipResponse;
    }

    public SkipContentTypeItemEntity() {
    }

    @Override
    public String getKey() {
        if (key == null) {
            key = contentType;
        }
        return key;
    }
}
