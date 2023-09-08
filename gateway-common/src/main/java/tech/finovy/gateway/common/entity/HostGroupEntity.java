package tech.finovy.gateway.common.entity;

import lombok.Data;

import java.util.List;

@Data
public class HostGroupEntity {
    private boolean directPass;
    private boolean validatorToken = true;
    private List<HostItemEntity> hosts;
}
