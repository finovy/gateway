package tech.finovy.gateway.common.entity;

import lombok.Data;

import java.util.List;

@Data
public class DegradeGroupEntity {
    private int degradeCode;
    private String degradeMessage;
    private int paramFlowCode;
    private String paramFlowMessage;
    private int systemBlockCode;
    private String systemBlockMessage;

    private String degradeApi;
    private String paramFlowApi;
    private String systemBlockApi;

    private List<DegradeEntity> config;
}
