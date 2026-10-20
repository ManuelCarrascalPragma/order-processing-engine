package co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    private String id;
    private String customerEmail;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String transactionId;

    public OrderJpaEntity() {}

    public OrderJpaEntity(String id, String customerEmail, BigDecimal amount, String currency, String status, String transactionId) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.transactionId = transactionId;
    }

    public String getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public String getTransactionId() { return transactionId; }

    public void setId(String id) { this.id = id; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setStatus(String status) { this.status = status; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
