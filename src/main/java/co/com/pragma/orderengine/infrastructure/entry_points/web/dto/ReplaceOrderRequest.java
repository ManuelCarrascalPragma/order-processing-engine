package co.com.pragma.orderengine.infrastructure.entry_points.web.dto;

import co.com.pragma.orderengine.domain.model.OrderStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReplaceOrderRequest(
        @NotNull @Email String email,
        @NotNull @Positive BigDecimal amount,
        @NotNull OrderStatus status,
        String transactionId
) {}
