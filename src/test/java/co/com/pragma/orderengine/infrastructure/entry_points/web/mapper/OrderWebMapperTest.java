package co.com.pragma.orderengine.infrastructure.entry_points.web.mapper;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.infrastructure.entry_points.web.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderWebMapperTest {

    private final IOrderWebMapper mapper = Mappers.getMapper(IOrderWebMapper.class);

    @Test
    void shouldMapDomainToResponse() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(100.0),
                OrderStatus.PAID, "tx-1");

        OrderResponse response = mapper.toResponse(order);

        assertEquals("ord-1", response.id());
        assertEquals("client@example.com", response.customerEmail());
        assertEquals(0, Money.ofUsd(100.0).amount().compareTo(response.amount()));
        assertEquals("USD", response.currency());
        assertEquals("PAID", response.status());
        assertEquals("tx-1", response.transactionId());
    }

    @Test
    void shouldReturnNullWhenOrderIsNull() {
        assertNull(mapper.toResponse(null));
    }
}
