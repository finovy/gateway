package tech.finovy.gateway.common.exception;

import lombok.Data;

@Data
public class DegradeLogEntity {
    private int code;
    private String msg;
    private String degradeApi;

    public DegradeLogEntity(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
