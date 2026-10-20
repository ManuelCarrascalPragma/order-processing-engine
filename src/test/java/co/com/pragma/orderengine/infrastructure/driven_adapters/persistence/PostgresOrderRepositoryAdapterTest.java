package co.com.pragma.orderengine.infrastructure.driven_adapters.persistence;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.entity.OrderJpaEntity;
import co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.mapper.IOrderPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgresOrderRepositoryAdapterTest {

    private SpringDataJpaOrderRepository jpaRepository;
    private PostgresOrderRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = Mockito.mock(SpringDataJpaOrderRepository.class);
        IOrderPersistenceMapper mapper = Mappers.getMapper(IOrderPersistenceMapper.class);
        adapter = new PostgresOrderRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    void shouldSaveOrderMappingToEntity() {
        Order order = new Order("ord-1", "client@example.com", Money.ofUsd(100.0));

        Order saved = adapter.save(order);

        assertEquals(order, saved);
        verify(jpaRepository).save(any(OrderJpaEntity.class));
    }

    @Test
    void shouldFindOrderByIdMappingToDomain() {
        OrderJpaEntity entity = new OrderJpaEntity("ord-1", "client@example.com",
                BigDecimal.valueOf(100.0), "USD", "PAID", "tx-1");
        when(jpaRepository.findById("ord-1")).thenReturn(Optional.of(entity));

        Optional<Order> result = adapter.findById("ord-1");

        assertTrue(result.isPresent());
        assertEquals("ord-1", result.get().getId());
        assertEquals(OrderStatus.PAID, result.get().getStatus());
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        assertTrue(adapter.findById("missing").isEmpty());
    }
}
