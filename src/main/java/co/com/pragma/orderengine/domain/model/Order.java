package co.com.pragma.orderengine.domain.model;

import co.com.pragma.orderengine.domain.model.exception.InvalidOrderStateException;

import java.util.Objects;

public class Order {
    private final String id;
    private final String customerEmail;
    private final Money totalAmount;
    private OrderStatus status;
    private String transactionId;

    public Order(String id, String customerEmail, Money totalAmount) {
        this.id = Objects.requireNonNull(id, "Order ID required");
        this.customerEmail = Objects.requireNonNull(customerEmail, "Email required");
        this.totalAmount = Objects.requireNonNull(totalAmount, "Amount required");
        this.status = OrderStatus.CREATED;
    }

    public Order(String id, String customerEmail, Money totalAmount, OrderStatus status, String transactionId) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.transactionId = transactionId;
    }

    public void validateCanBeProcessed() {
        if (this.status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("Order cannot be processed in status: " + this.status);
        }
    }

    public void markAsPaid(String transactionId) {
        if (this.status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("Cannot pay an order that is not CREATED");
        }
        this.status = OrderStatus.PAID;
        this.transactionId = transactionId;
    }

    public String getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public Money getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public String getTransactionId() { return transactionId; }
}
