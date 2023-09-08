package tech.finovy.gateway.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExceptionEvent extends RequestEvent {
    private String errMsg;
    private String exceptionType;
    private String statusCode;
    private String routeId;
    private int count;
}
