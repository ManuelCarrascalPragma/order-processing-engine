package co.com.pragma.orderengine.domain.model;

import co.com.pragma.orderengine.domain.model.exception.InvalidOrderStateException;
import co.com.pragma.orderengine.domain.model.exception.VersionConflictException;

import java.util.Objects;

public class Order {
    private final String id;
    private String customerEmail;
    private Money totalAmount;
    private OrderStatus status;
    private String transactionId;
 
    private Long version;

    public Order(String id, String customerEmail, Money totalAmount) {
        this.id = Objects.requireNonNull(id, "Order ID required");
        this.customerEmail = Objects.requireNonNull(customerEmail, "Email required");
        this.totalAmount = Objects.requireNonNull(totalAmount, "Amount required");
        this.status = OrderStatus.CREATED;
    }

    public Order(String id, String customerEmail, Money totalAmount, OrderStatus status, String transactionId) {
        this(id, customerEmail, totalAmount, status, transactionId, null);
    }

    public Order(String id, String customerEmail, Money totalAmount, OrderStatus status,
                 String transactionId, Long version) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.transactionId = transactionId;
        this.version = version;
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

    public void ensureVersionMatches(Long expectedVersion) {
        if (expectedVersion == null || !expectedVersion.equals(this.version)) {
            throw new VersionConflictException(
                    "Version mismatch for order " + this.id
                            + ": expected " + expectedVersion + " but current is " + this.version);
        }
    }

    public void changeStatus(OrderStatus newStatus) {
        Objects.requireNonNull(newStatus, "New status required");
        if (this.status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("A CANCELLED order cannot change status");
        }
        if (newStatus == OrderStatus.PAID && this.status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("Only a CREATED order can be marked as PAID");
        }
        this.status = newStatus;
    }

  
    public void replaceWith(String customerEmail, Money totalAmount, OrderStatus status, String transactionId) {
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.transactionId = transactionId;
    }

    public String getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public Money getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public String getTransactionId() { return transactionId; }
    public Long getVersion() { return version; }
}
