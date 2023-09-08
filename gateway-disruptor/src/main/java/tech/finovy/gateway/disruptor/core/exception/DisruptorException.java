package tech.finovy.gateway.disruptor.core.exception;

public class DisruptorException extends RuntimeException {
    private static final long serialVersionUID = 4902033891210380143L;

    public DisruptorException(Throwable cause) {
        super(cause);
    }

    public DisruptorException(String message) {
        super(message);
    }
}
