package co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "idempotency_record")
public class IdempotencyRecordJpaEntity {

    @Id
    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String key;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Lob
    @Column(name = "response_body", nullable = false)
    private String responseBody;

    public IdempotencyRecordJpaEntity() {}

    public IdempotencyRecordJpaEntity(String key, Integer statusCode, String responseBody) {
        this.key = key;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public String getKey() { return key; }
    public Integer getStatusCode() { return statusCode; }
    public String getResponseBody() { return responseBody; }

    public void setKey(String key) { this.key = key; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
}
