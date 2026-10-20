package co.com.pragma.orderengine.domain.model;

import co.com.pragma.orderengine.domain.model.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void shouldCreateOrderInCreatedStatus() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0));

        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals("ord-1", order.getId());
        assertEquals("client@example.com", order.getCustomerEmail());
    }

    @Test
    void shouldValidateWhenOrderIsCreated() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0));

        assertDoesNotThrow(order::validateCanBeProcessed);
    }

    @Test
    void shouldMarkOrderAsPaid() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0));

        order.markAsPaid("tx-1");

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("tx-1", order.getTransactionId());
    }

    @Test
    void shouldRejectValidationWhenNotCreated() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0), OrderStatus.PAID, "tx-1");

        assertThrows(InvalidOrderStateException.class, order::validateCanBeProcessed);
    }

    @Test
    void shouldRejectPayingAnOrderNotInCreatedStatus() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0), OrderStatus.PAID, "tx-1");

        assertThrows(InvalidOrderStateException.class, () -> order.markAsPaid("tx-2"));
    }

    @Test
    void shouldRejectNullIdOnCreation() {
        assertThrows(NullPointerException.class,
                () -> new Order(null, "client@example.com", Money.ofUsd(10.0)));
    }
}
