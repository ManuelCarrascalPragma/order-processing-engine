package co.com.pragma.orderengine.infrastructure.entry_points.web.dto;

import co.com.pragma.orderengine.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status
) {}
