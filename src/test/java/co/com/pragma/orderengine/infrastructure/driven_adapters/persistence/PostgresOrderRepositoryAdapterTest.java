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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        OrderJpaEntity persisted = new OrderJpaEntity("ord-1", "client@example.com",
                BigDecimal.valueOf(100.0), "USD", "CREATED", null);
        when(jpaRepository.save(any(OrderJpaEntity.class))).thenReturn(persisted);

        Order saved = adapter.save(order);

        assertEquals("ord-1", saved.getId());
        assertEquals(OrderStatus.CREATED, saved.getStatus());
        verify(jpaRepository).save(any(OrderJpaEntity.class));
    }

    @Test
    void shouldUpdateOrderAndMapResultBack() {
        Order order = new Order("ord-9", "client@example.com", Money.ofUsd(100.0),
                OrderStatus.PAID, "tx-1", 2L);
        OrderJpaEntity persisted = new OrderJpaEntity("ord-9", "client@example.com",
                BigDecimal.valueOf(100.0), "USD", "PAID", "tx-1");
        when(jpaRepository.saveAndFlush(any(OrderJpaEntity.class))).thenReturn(persisted);

        Order updated = adapter.update(order);

        assertEquals("ord-9", updated.getId());
        assertEquals(OrderStatus.PAID, updated.getStatus());
        verify(jpaRepository).saveAndFlush(any(OrderJpaEntity.class));
    }

    @Test
    void shouldTranslateOptimisticLockFailureToVersionConflict() {
        Order order = new Order("ord-x", "client@example.com", Money.ofUsd(100.0),
                OrderStatus.PAID, "tx-1", 1L);
        when(jpaRepository.saveAndFlush(any(OrderJpaEntity.class)))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException(
                        OrderJpaEntity.class, "ord-x"));

        assertThrows(
                co.com.pragma.orderengine.domain.model.exception.VersionConflictException.class,
                () -> adapter.update(order));
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
