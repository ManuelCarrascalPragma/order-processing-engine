package co.com.pragma.orderengine.domain.usecases;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.domain.model.exception.OrderNotFoundException;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReplaceOrderUseCaseImplTest {

    private IOrderRepositoryPort orderRepository;
    private ReplaceOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(IOrderRepositoryPort.class);
        useCase = new ReplaceOrderUseCaseImpl(orderRepository);
    }

    @Test
    void shouldReplaceAllFieldsOfExistingOrder() {
        Order existing = new Order("ord-1", "old@example.com", Money.ofUsd(50.0),
                OrderStatus.CREATED, null, 1L);
        when(orderRepository.findById("ord-1")).thenReturn(Optional.of(existing));
        when(orderRepository.update(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order replacement = new Order("ord-1", "new@example.com", Money.ofUsd(200.0),
                OrderStatus.PAID, "tx-new");

        Order result = useCase.execute(replacement);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).update(captor.capture());
        Order persisted = captor.getValue();
        assertEquals("new@example.com", persisted.getCustomerEmail());
        assertEquals(OrderStatus.PAID, persisted.getStatus());
        assertEquals("tx-new", persisted.getTransactionId());
        assertEquals("ord-1", result.getId());
    }

    @Test
    void shouldThrowNotFoundWhenTargetMissing() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());
        Order replacement = new Order("missing", "new@example.com", Money.ofUsd(200.0),
                OrderStatus.PAID, "tx-new");

        assertThrows(OrderNotFoundException.class, () -> useCase.execute(replacement));
        verify(orderRepository, never()).update(any(Order.class));
    }
}
