package co.com.pragma.orderengine.infrastructure.entry_points.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotNull @Email String email,
        @NotNull @Positive BigDecimal amount
) {}
