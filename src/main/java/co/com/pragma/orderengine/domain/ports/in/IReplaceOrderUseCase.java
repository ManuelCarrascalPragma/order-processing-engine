package co.com.pragma.orderengine.domain.ports.in;

import co.com.pragma.orderengine.domain.model.Order;

public interface IReplaceOrderUseCase {

    Order execute(Order replacement);
}
