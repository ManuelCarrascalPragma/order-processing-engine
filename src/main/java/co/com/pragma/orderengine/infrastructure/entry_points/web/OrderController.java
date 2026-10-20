package co.com.pragma.orderengine.infrastructure.entry_points.web;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.ports.in.ICheckoutOrderUseCase;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import co.com.pragma.orderengine.infrastructure.entry_points.web.dto.CreateOrderRequest;
import co.com.pragma.orderengine.infrastructure.entry_points.web.dto.OrderResponse;
import co.com.pragma.orderengine.infrastructure.entry_points.web.mapper.IOrderWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final ICheckoutOrderUseCase checkoutOrderUseCase;
    private final IOrderRepositoryPort orderRepository;
    private final IOrderWebMapper mapper;

    public OrderController(ICheckoutOrderUseCase checkoutOrderUseCase,
                           IOrderRepositoryPort orderRepository,
                           IOrderWebMapper mapper) {
        this.checkoutOrderUseCase = checkoutOrderUseCase;
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@Valid @RequestBody CreateOrderRequest request) {
        Order order = new Order(
                UUID.randomUUID().toString(),
                request.email(),
                Money.ofUsd(request.amount().doubleValue())
        );

        checkoutOrderUseCase.execute(order);
        return mapper.toResponse(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
