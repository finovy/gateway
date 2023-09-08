package tech.finovy.gateway.disruptor.core.event;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

public class DisruptorEvent<T extends Serializable> implements Serializable {
    @Serial
    private static final long serialVersionUID = 2536114444981904368L;
    private String transactionId;
    private long eventId;
    private String disruptorType;
    private String eventTopic;
    private String eventTags;
    private String application;
    private int eventRetryCount;
    private transient Set<String> executeListener;
    private T t;

    public Set<String> getExecuteListener() {
        return executeListener;
    }

    public void setExecuteListener(Set<String> executeListener) {
        this.executeListener = executeListener;
    }

    public int getEventRetryCount() {
        return eventRetryCount;
    }

    public void setEventRetryCount(int eventRetryCount) {
        this.eventRetryCount = eventRetryCount;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getEventTags() {
        return eventTags;
    }

    public void setEventTags(String eventTags) {
        this.eventTags = eventTags;
    }

    public String getEventTopic() {
        return eventTopic;
    }

    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getDisruptorEventType() {
        return disruptorType;
    }

    public void setDisruptorEventType(String type) {
        this.disruptorType = type;
    }

    public void setDisruptorEvent(T event) {
        this.t = event;
    }

    public T getEvent() {
        return t;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
