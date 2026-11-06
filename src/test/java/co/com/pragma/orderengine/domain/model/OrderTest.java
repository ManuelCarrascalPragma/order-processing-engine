package co.com.pragma.orderengine.domain.model;

import co.com.pragma.orderengine.domain.model.exception.InvalidOrderStateException;
import co.com.pragma.orderengine.domain.model.exception.VersionConflictException;
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


    @Test
    void shouldPassWhenVersionMatches() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0),
                OrderStatus.CREATED, null, 3L);

        assertDoesNotThrow(() -> order.ensureVersionMatches(3L));
    }

    @Test
    void shouldFailWhenVersionDoesNotMatch() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0),
                OrderStatus.CREATED, null, 3L);

        assertThrows(VersionConflictException.class, () -> order.ensureVersionMatches(1L));
    }

    @Test
    void shouldFailWhenExpectedVersionIsNull() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0),
                OrderStatus.CREATED, null, 3L);

        assertThrows(VersionConflictException.class, () -> order.ensureVersionMatches(null));
    }


    @Test
    void shouldChangeStatusFromCreatedToPaid() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0));

        order.changeStatus(OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void shouldChangeStatusToCancelled() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0));

        order.changeStatus(OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void shouldRejectChangingStatusOfCancelledOrder() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0),
                OrderStatus.CANCELLED, null, 0L);

        assertThrows(InvalidOrderStateException.class, () -> order.changeStatus(OrderStatus.PAID));
    }

    @Test
    void shouldRejectMarkingPaidWhenNotCreated() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0),
                OrderStatus.PAID, "tx-1", 0L);

        assertThrows(InvalidOrderStateException.class, () -> order.changeStatus(OrderStatus.PAID));
    }

    @Test
    void shouldRejectNullStatusOnChange() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(10.0));

        assertThrows(NullPointerException.class, () -> order.changeStatus(null));
    }


    @Test
    void shouldReplaceAllMutableFields() {
        Order order = new Order("ord-1", "old@example.com", Money.ofUsd(10.0),
                OrderStatus.CREATED, null, 2L);

        order.replaceWith("new@example.com", Money.ofUsd(99.0), OrderStatus.PAID, "tx-9");

        assertEquals("new@example.com", order.getCustomerEmail());
        assertEquals(0, Money.ofUsd(99.0).amount().compareTo(order.getTotalAmount().amount()));
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("tx-9", order.getTransactionId());
        assertEquals("ord-1", order.getId());
        assertEquals(2L, order.getVersion());
    }
}
