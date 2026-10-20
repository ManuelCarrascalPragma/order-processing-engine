package co.com.pragma.orderengine.domain.ports.out;

import co.com.pragma.orderengine.domain.model.Order;

import java.util.Optional;

public interface IOrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(String id);
}
