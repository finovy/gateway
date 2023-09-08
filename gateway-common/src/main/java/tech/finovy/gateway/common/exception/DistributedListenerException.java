package tech.finovy.gateway.common.exception;

public class DistributedListenerException extends RuntimeException {

    private static final long serialVersionUID = 4707127779619100807L;

    public DistributedListenerException(Throwable cause) {
        super(cause);
    }

    public DistributedListenerException(String message) {
        super(message);
    }
}
