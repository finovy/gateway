package tech.finovy.gateway.common.entity;

import org.springframework.util.MultiValueMap;

import java.io.Serializable;

public class EventTransaction implements Serializable {


    private String transactionId;
    private String application;
    private long timestamp;
    private MultiValueMap<String, String> requestHeaders;

    public EventTransaction() {
    }


    public MultiValueMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRquestHeaders(MultiValueMap<String, String> headers) {
        this.requestHeaders = headers;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = String.valueOf(transactionId);
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
