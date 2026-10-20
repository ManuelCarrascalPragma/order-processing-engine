package co.com.pragma.orderengine.domain.usecases;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.domain.model.exception.PaymentFailedException;
import co.com.pragma.orderengine.domain.ports.out.IChargeablePaymentGateway;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import co.com.pragma.orderengine.domain.ports.out.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutOrderUseCaseImplTest {

    private IOrderRepositoryPort orderRepository;
    private IChargeablePaymentGateway paymentGateway;
    private CheckoutOrderUseCaseImpl checkoutOrderUseCase;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(IOrderRepositoryPort.class);
        paymentGateway = Mockito.mock(IChargeablePaymentGateway.class);
        checkoutOrderUseCase = new CheckoutOrderUseCaseImpl(orderRepository, paymentGateway);
    }

    @Test
    void shouldSuccessfullyProcessOrderCheckout() {
        Order order = new Order("ord-123", "client@example.com", Money.ofUsd(150.0));
        when(paymentGateway.charge(any(Money.class))).thenReturn(new PaymentResult(true, "tx-999"));

        checkoutOrderUseCase.execute(order);

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("tx-999", order.getTransactionId());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldThrowPaymentFailedExceptionWhenPaymentIsNotSuccessful() {
        Order order = new Order("ord-456", "client@example.com", Money.ofUsd(200.0));
        when(paymentGateway.charge(any(Money.class))).thenReturn(new PaymentResult(false, null));

        assertThrows(PaymentFailedException.class, () -> checkoutOrderUseCase.execute(order));

        assertEquals(OrderStatus.CREATED, order.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }
}
