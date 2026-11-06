package co.com.pragma.orderengine.domain.usecases;

import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.exception.OrderNotFoundException;
import co.com.pragma.orderengine.domain.ports.in.IReplaceOrderUseCase;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;

public class ReplaceOrderUseCaseImpl implements IReplaceOrderUseCase {

    private final IOrderRepositoryPort orderRepository;

    public ReplaceOrderUseCaseImpl(IOrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order execute(Order replacement) {
        Order existing = orderRepository.findById(replacement.getId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + replacement.getId()));

        existing.replaceWith(
                replacement.getCustomerEmail(),
                replacement.getTotalAmount(),
                replacement.getStatus(),
                replacement.getTransactionId()
        );

        return orderRepository.update(existing);
    }
}
