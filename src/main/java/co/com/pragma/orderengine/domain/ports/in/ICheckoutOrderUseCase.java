package co.com.pragma.orderengine.domain.ports.in;

import co.com.pragma.orderengine.domain.model.Order;

public interface ICheckoutOrderUseCase {
    void execute(Order order);
}
