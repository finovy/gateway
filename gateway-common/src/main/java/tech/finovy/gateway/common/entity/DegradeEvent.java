package tech.finovy.gateway.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DegradeEvent extends RequestEvent {
    private String msg;
    private String exceptionMsg;
    private String exceptionType;
    private int code;
    private String routeId;
    private String traceId;
    private String degradeApi;
    private int count;

    public DegradeEvent(int code, String msg, String routeId, String traceId) {
        this.msg = msg;
        this.code = code;
        this.routeId = routeId;
        this.traceId = traceId;
    }
}
