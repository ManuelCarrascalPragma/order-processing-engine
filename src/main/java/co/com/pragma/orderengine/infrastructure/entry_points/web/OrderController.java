package co.com.pragma.orderengine.infrastructure.entry_points.web;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.IdempotencyRecord;
import co.com.pragma.orderengine.domain.ports.in.ICheckoutOrderUseCase;
import co.com.pragma.orderengine.domain.ports.in.IReplaceOrderUseCase;
import co.com.pragma.orderengine.domain.ports.in.IUpdateOrderStatusUseCase;
import co.com.pragma.orderengine.domain.ports.out.IIdempotencyStorePort;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import co.com.pragma.orderengine.infrastructure.entry_points.web.dto.CreateOrderRequest;
import co.com.pragma.orderengine.infrastructure.entry_points.web.dto.OrderResponse;
import co.com.pragma.orderengine.infrastructure.entry_points.web.dto.ReplaceOrderRequest;
import co.com.pragma.orderengine.infrastructure.entry_points.web.dto.UpdateOrderStatusRequest;
import co.com.pragma.orderengine.infrastructure.entry_points.web.mapper.IOrderWebMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final ICheckoutOrderUseCase checkoutOrderUseCase;
    private final IUpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final IReplaceOrderUseCase replaceOrderUseCase;
    private final IOrderRepositoryPort orderRepository;
    private final IIdempotencyStorePort idempotencyStore;
    private final IOrderWebMapper mapper;
    private final ObjectMapper objectMapper;

    public OrderController(ICheckoutOrderUseCase checkoutOrderUseCase,
                           IUpdateOrderStatusUseCase updateOrderStatusUseCase,
                           IReplaceOrderUseCase replaceOrderUseCase,
                           IOrderRepositoryPort orderRepository,
                           IIdempotencyStorePort idempotencyStore,
                           IOrderWebMapper mapper,
                           ObjectMapper objectMapper) {
        this.checkoutOrderUseCase = checkoutOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.replaceOrderUseCase = replaceOrderUseCase;
        this.orderRepository = orderRepository;
        this.idempotencyStore = idempotencyStore;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {

        Optional<IdempotencyRecord> existing = idempotencyStore.findByKey(idempotencyKey);
        if (existing.isPresent()) {
            return replay(existing.get());
        }

        Order order = new Order(
                UUID.randomUUID().toString(),
                request.email(),
                Money.ofUsd(request.amount().doubleValue())
        );
        Order processed = checkoutOrderUseCase.execute(order);
        OrderResponse body = mapper.toResponse(processed);

        idempotencyStore.save(new IdempotencyRecord(
                idempotencyKey, HttpStatus.CREATED.value(), serialize(body)));

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable String id,
            @RequestHeader(value = "If-Match") Long version,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        Order updated = updateOrderStatusUseCase.execute(id, request.status(), version);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> replaceOrder(
            @PathVariable String id,
            @Valid @RequestBody ReplaceOrderRequest request) {

        Order replacement = new Order(
                id,
                request.email(),
                Money.ofUsd(request.amount().doubleValue()),
                request.status(),
                request.transactionId()
        );
        Order replaced = replaceOrderUseCase.execute(replacement);
        return ResponseEntity.ok(mapper.toResponse(replaced));
    }

    private ResponseEntity<OrderResponse> replay(IdempotencyRecord record) {
        OrderResponse body = deserialize(record.responseBody());
        return ResponseEntity.status(record.statusCode()).body(body);
    }

    private String serialize(OrderResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize idempotent response", e);
        }
    }

    private OrderResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, OrderResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize idempotent response", e);
        }
    }
}
