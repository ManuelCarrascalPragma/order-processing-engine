package co.com.pragma.orderengine.domain.model.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainExceptionsTest {

    @Test
    void paymentFailedExceptionShouldCarryMessage() {
        PaymentFailedException ex = new PaymentFailedException("payment failed");

        assertEquals("payment failed", ex.getMessage());
    }

    @Test
    void invalidOrderStateExceptionShouldCarryMessage() {
        InvalidOrderStateException ex = new InvalidOrderStateException("invalid state");

        assertEquals("invalid state", ex.getMessage());
    }

    @Test
    void versionConflictExceptionShouldCarryMessage() {
        VersionConflictException ex = new VersionConflictException("version conflict");

        assertEquals("version conflict", ex.getMessage());
    }

    @Test
    void orderNotFoundExceptionShouldCarryMessage() {
        OrderNotFoundException ex = new OrderNotFoundException("not found");

        assertEquals("not found", ex.getMessage());
    }
}
