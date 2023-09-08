package tech.finovy.gateway.common.entity;

import lombok.Data;

@Data
public class HostItemEntity {
    private String host;
    private String appId;
    private int order = 0;
    private boolean directPass;
    private boolean validatorToken = false;
    private boolean exists = false;
}
