package co.com.pragma.orderengine.domain.ports.out;

public record PaymentResult(boolean isSuccess, String transactionId) {}
