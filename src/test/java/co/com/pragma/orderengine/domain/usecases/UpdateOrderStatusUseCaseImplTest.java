package co.com.pragma.orderengine.domain.usecases;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.domain.model.exception.OrderNotFoundException;
import co.com.pragma.orderengine.domain.model.exception.VersionConflictException;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateOrderStatusUseCaseImplTest {

    private IOrderRepositoryPort orderRepository;
    private UpdateOrderStatusUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(IOrderRepositoryPort.class);
        useCase = new UpdateOrderStatusUseCaseImpl(orderRepository);
    }

    @Test
    void shouldUpdateStatusWhenVersionMatches() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(100.0),
                OrderStatus.CREATED, null, 1L);
        when(orderRepository.findById("ord-1")).thenReturn(Optional.of(order));
        when(orderRepository.update(any(Order.class))).thenReturn(order);

        Order result = useCase.execute("ord-1", OrderStatus.CANCELLED, 1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository).update(order);
    }

    @Test
    void shouldThrowNotFoundWhenOrderMissing() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> useCase.execute("missing", OrderStatus.CANCELLED, 1L));
        verify(orderRepository, never()).update(any(Order.class));
    }

    @Test
    void shouldThrowVersionConflictWhenVersionMismatch() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(100.0),
                OrderStatus.CREATED, null, 5L);
        when(orderRepository.findById("ord-1")).thenReturn(Optional.of(order));

        assertThrows(VersionConflictException.class,
                () -> useCase.execute("ord-1", OrderStatus.CANCELLED, 1L));
        verify(orderRepository, never()).update(any(Order.class));
    }
}
