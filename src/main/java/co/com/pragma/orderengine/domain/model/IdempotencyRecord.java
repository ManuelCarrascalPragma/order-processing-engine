package co.com.pragma.orderengine.domain.model;

import java.util.Objects;

public record IdempotencyRecord(String key, int statusCode, String responseBody) {

    public IdempotencyRecord {
        Objects.requireNonNull(key, "Idempotency key required");
        Objects.requireNonNull(responseBody, "Response body required");
    }
}
