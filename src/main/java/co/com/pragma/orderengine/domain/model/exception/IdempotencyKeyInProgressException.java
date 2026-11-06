package co.com.pragma.orderengine.domain.model.exception;

public class IdempotencyKeyInProgressException extends RuntimeException {
    public IdempotencyKeyInProgressException(String message) {
        super(message);
    }
}
