package tech.finovy.gateway.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.MultiValueMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResponseEvent extends EventTransaction {
    private String statusCode;
    private String routeId;
    private long spends;
    private String filterType;
    private byte[] body;
    private String contentType;
    private MultiValueMap<String, String> responseHeaders;

    public MultiValueMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(MultiValueMap<String, String> headers) {
        this.responseHeaders = headers;
    }
}
