package tech.finovy.gateway.common.entity;

import lombok.Data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class SkipUrlGroupEntity {
    private boolean directPass;
    private boolean skipLog;
    private boolean skipAuth;
    private boolean skipResponse;
    private boolean skipRequest;
    private boolean skipEncry;
    private boolean skipDecry;
    private List<SkipUrlItemEntity> skips;
    private List<String> headers;
    private List<SkipContentTypeItemEntity> contentTypes;

    public void setSkips(List<SkipUrlItemEntity> skips) {
        this.skips = skips;
        sort(skips);
    }

    public void setContentTypes(List<SkipContentTypeItemEntity> contentTypes) {
        this.contentTypes = contentTypes;
        sort(contentTypes);
    }

    private void sort(List<? extends SkipItemEntity> list) {
        Collections.sort(list, Comparator.comparingInt(SkipItemEntity::getOrder));
    }
}
