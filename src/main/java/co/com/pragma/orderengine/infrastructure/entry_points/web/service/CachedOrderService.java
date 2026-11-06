package co.com.pragma.orderengine.infrastructure.entry_points.web.service;

import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.domain.model.exception.OrderNotFoundException;
import co.com.pragma.orderengine.domain.ports.in.IReplaceOrderUseCase;
import co.com.pragma.orderengine.domain.ports.in.IUpdateOrderStatusUseCase;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import co.com.pragma.orderengine.infrastructure.entry_points.web.dto.OrderResponse;
import co.com.pragma.orderengine.infrastructure.entry_points.web.mapper.IOrderWebMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachedOrderService {

    private static final Logger log = LoggerFactory.getLogger(CachedOrderService.class);

    public static final String ORDERS_CACHE = "orders";

    private final IOrderRepositoryPort orderRepository;
    private final IUpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final IReplaceOrderUseCase replaceOrderUseCase;
    private final IOrderWebMapper mapper;

    public CachedOrderService(IOrderRepositoryPort orderRepository,
                              IUpdateOrderStatusUseCase updateOrderStatusUseCase,
                              IReplaceOrderUseCase replaceOrderUseCase,
                              IOrderWebMapper mapper) {
        this.orderRepository = orderRepository;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.replaceOrderUseCase = replaceOrderUseCase;
        this.mapper = mapper;
    }

    @Cacheable(value = ORDERS_CACHE, key = "#id")
    public OrderResponse getById(String id) {
        log.debug("Cache MISS orders::{} -> consultando base de datos", id);
        return orderRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
    }

    @CachePut(value = ORDERS_CACHE, key = "#id")
    public OrderResponse updateStatus(String id, OrderStatus status, Long expectedVersion) {
        Order updated = updateOrderStatusUseCase.execute(id, status, expectedVersion);
        log.debug("Cache PUT orders::{} (PATCH write-through)", id);
        return mapper.toResponse(updated);
    }

    @CachePut(value = ORDERS_CACHE, key = "#id")
    public OrderResponse replace(String id, Order replacement) {
        Order replaced = replaceOrderUseCase.execute(replacement);
        log.debug("Cache PUT orders::{} (PUT write-through)", id);
        return mapper.toResponse(replaced);
    }
}
