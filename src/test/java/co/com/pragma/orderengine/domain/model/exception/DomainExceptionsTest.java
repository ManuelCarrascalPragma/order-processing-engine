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
}
