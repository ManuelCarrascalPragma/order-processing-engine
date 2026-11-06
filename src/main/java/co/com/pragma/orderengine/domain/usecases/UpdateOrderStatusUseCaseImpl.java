package co.com.pragma.orderengine.domain.usecases;

import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.domain.model.exception.OrderNotFoundException;
import co.com.pragma.orderengine.domain.ports.in.IUpdateOrderStatusUseCase;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;

public class UpdateOrderStatusUseCaseImpl implements IUpdateOrderStatusUseCase {

    private final IOrderRepositoryPort orderRepository;

    public UpdateOrderStatusUseCaseImpl(IOrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order execute(String orderId, OrderStatus newStatus, Long expectedVersion) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        order.ensureVersionMatches(expectedVersion);

        order.changeStatus(newStatus);

        return orderRepository.update(order);
    }
}
