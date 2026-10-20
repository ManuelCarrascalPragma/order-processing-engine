package co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.mapper;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.entity.OrderJpaEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderPersistenceMapperTest {

    private final IOrderPersistenceMapper mapper = Mappers.getMapper(IOrderPersistenceMapper.class);

    @Test
    void shouldMapDomainToEntity() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(100.0),
                OrderStatus.PAID, "tx-1");

        OrderJpaEntity entity = mapper.toEntity(order);

        assertEquals("ord-1", entity.getId());
        assertEquals("client@example.com", entity.getCustomerEmail());
        assertEquals(0, Money.ofUsd(100.0).amount().compareTo(entity.getAmount()));
        assertEquals("USD", entity.getCurrency());
        assertEquals("PAID", entity.getStatus());
        assertEquals("tx-1", entity.getTransactionId());
    }

    @Test
    void shouldMapEntityToDomain() {
        OrderJpaEntity entity = new OrderJpaEntity("ord-2", "buyer@example.com",
                java.math.BigDecimal.valueOf(50.0), "USD", "CREATED", null);

        Order order = mapper.toDomain(entity);

        assertEquals("ord-2", order.getId());
        assertEquals("buyer@example.com", order.getCustomerEmail());
        assertEquals(0, java.math.BigDecimal.valueOf(50.0).compareTo(order.getTotalAmount().amount()));
        assertEquals("USD", order.getTotalAmount().currency().getCurrencyCode());
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(mapper.toEntity(null));
    }
}
