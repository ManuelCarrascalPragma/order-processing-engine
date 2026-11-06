package co.com.pragma.orderengine.domain.ports.in;

import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;

public interface IUpdateOrderStatusUseCase {

    Order execute(String orderId, OrderStatus newStatus, Long expectedVersion);
}
