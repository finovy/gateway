package tech.finovy.gateway.exception;

public class DistributedListenerException extends RuntimeException {

    private static final long serialVersionUID = 4707127779619100807L;

    public DistributedListenerException(Throwable cause) {
        super(cause);
    }

    public DistributedListenerException(String message) {
        super(message);
    }
}
