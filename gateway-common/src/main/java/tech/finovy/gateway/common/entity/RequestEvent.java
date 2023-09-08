package tech.finovy.gateway.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RequestEvent extends EventTransaction {
    private byte[] requestBody;
    private String contentType;
    private String url;
    private String remoteAddress;
    private String httpMethod;
    private String scheme;
    private String host;
    private int port;
    private String requestId;

    public RequestEvent() {
    }

    public RequestEvent(byte[] requestBody) {
        this.requestBody = requestBody;
    }
}
