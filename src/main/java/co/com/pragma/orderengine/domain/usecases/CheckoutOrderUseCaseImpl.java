package co.com.pragma.orderengine.domain.usecases;

import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.exception.PaymentFailedException;
import co.com.pragma.orderengine.domain.ports.in.ICheckoutOrderUseCase;
import co.com.pragma.orderengine.domain.ports.out.IChargeablePaymentGateway;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import co.com.pragma.orderengine.domain.ports.out.PaymentResult;

public class CheckoutOrderUseCaseImpl implements ICheckoutOrderUseCase {

    private final IOrderRepositoryPort orderRepository;
    private final IChargeablePaymentGateway paymentGateway;

    public CheckoutOrderUseCaseImpl(IOrderRepositoryPort orderRepository,
                                    IChargeablePaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    @Override
    public void execute(Order order) {
        order.validateCanBeProcessed();

        PaymentResult paymentResult = paymentGateway.charge(order.getTotalAmount());
        if (!paymentResult.isSuccess()) {
            throw new PaymentFailedException("Payment processing failed for order: " + order.getId());
        }

        order.markAsPaid(paymentResult.transactionId());
        orderRepository.save(order);
    }
}
