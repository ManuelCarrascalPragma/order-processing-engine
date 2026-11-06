package co.com.pragma.orderengine.domain.model.exception;

public class VersionConflictException extends RuntimeException {
    public VersionConflictException(String message) {
        super(message);
    }
}
