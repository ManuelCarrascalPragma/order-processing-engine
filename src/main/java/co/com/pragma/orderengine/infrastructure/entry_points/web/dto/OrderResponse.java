package co.com.pragma.orderengine.infrastructure.entry_points.web.dto;

import java.math.BigDecimal;

public record OrderResponse(
        String id,
        String customerEmail,
        BigDecimal amount,
        String currency,
        String status,
        String transactionId
) {}
