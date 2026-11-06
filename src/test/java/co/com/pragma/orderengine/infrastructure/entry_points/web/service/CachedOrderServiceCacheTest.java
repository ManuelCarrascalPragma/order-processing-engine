package co.com.pragma.orderengine.infrastructure.entry_points.web.service;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.domain.model.exception.OrderNotFoundException;
import co.com.pragma.orderengine.domain.ports.in.IReplaceOrderUseCase;
import co.com.pragma.orderengine.domain.ports.in.IUpdateOrderStatusUseCase;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import co.com.pragma.orderengine.infrastructure.entry_points.web.dto.OrderResponse;
import co.com.pragma.orderengine.infrastructure.entry_points.web.mapper.IOrderWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica el COMPORTAMIENTO de las estrategias de cache con los proxies reales
 * de Spring (sustituyendo Redis por una cache en memoria, ya que la logica de
 * @Cacheable / @CachePut es independiente del proveedor).
 */
@SpringJUnitConfig(CachedOrderServiceCacheTest.CacheTestConfig.class)
class CachedOrderServiceCacheTest {

    @Configuration
    @EnableCaching
    static class CacheTestConfig {
        @Bean CacheManager cacheManager() { return new ConcurrentMapCacheManager(CachedOrderService.ORDERS_CACHE); }
        @Bean IOrderRepositoryPort orderRepository() { return Mockito.mock(IOrderRepositoryPort.class); }
        @Bean IUpdateOrderStatusUseCase updateUseCase() { return Mockito.mock(IUpdateOrderStatusUseCase.class); }
        @Bean IReplaceOrderUseCase replaceUseCase() { return Mockito.mock(IReplaceOrderUseCase.class); }
        @Bean IOrderWebMapper mapper() { return Mappers.getMapper(IOrderWebMapper.class); }

        @Bean
        CachedOrderService cachedOrderService(IOrderRepositoryPort repo, IUpdateOrderStatusUseCase update,
                                              IReplaceOrderUseCase replace, IOrderWebMapper mapper) {
            return new CachedOrderService(repo, update, replace, mapper);
        }
    }

    @Autowired private CachedOrderService service;
    @Autowired private IOrderRepositoryPort repository;
    @Autowired private IUpdateOrderStatusUseCase updateUseCase;
    @Autowired private IReplaceOrderUseCase replaceUseCase;
    @Autowired private CacheManager cacheManager;

    @BeforeEach
    void resetState() {
        cacheManager.getCache(CachedOrderService.ORDERS_CACHE).clear();
        Mockito.reset(repository, updateUseCase, replaceUseCase);
    }

    private Order order(OrderStatus status, long version) {
        return new Order("ord-1", "client@example.com", Money.ofUsd(100.0), status, null, version);
    }

    @Test
    void cacheAside_secondGetIsServedFromCacheWithoutHittingDatabase() {
        when(repository.findById("ord-1")).thenReturn(Optional.of(order(OrderStatus.PAID, 0L)));

        OrderResponse first = service.getById("ord-1");
        OrderResponse second = service.getById("ord-1");

        assertEquals(first, second);
        verify(repository, times(1)).findById("ord-1");
        assertNotNull(cacheManager.getCache(CachedOrderService.ORDERS_CACHE).get("ord-1"));
    }

    @Test
    void cacheAside_notFoundIsNotCached() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.getById("missing"));
        assertThrows(OrderNotFoundException.class, () -> service.getById("missing"));

        verify(repository, times(2)).findById("missing");
    }

    @Test
    void writeThrough_patchOverwritesCachedEntryWithNewVersion() {
        when(repository.findById("ord-1")).thenReturn(Optional.of(order(OrderStatus.CREATED, 0L)));
        when(updateUseCase.execute("ord-1", OrderStatus.CANCELLED, 0L))
                .thenReturn(order(OrderStatus.CANCELLED, 1L));

        service.getById("ord-1");                                   // cache: CREATED v0
        service.updateStatus("ord-1", OrderStatus.CANCELLED, 0L);   // cache: CANCELLED v1
        OrderResponse afterPatch = service.getById("ord-1");        // sin ir a la BD

        assertEquals("CANCELLED", afterPatch.status());
        assertEquals(1L, afterPatch.version());
        verify(repository, times(1)).findById("ord-1");
    }

    @Test
    void writeThrough_putOverwritesCachedEntry() {
        Order replaced = new Order("ord-1", "new@example.com", Money.ofUsd(500.0),
                OrderStatus.PAID, "tx-9", 2L);
        when(replaceUseCase.execute(any(Order.class))).thenReturn(replaced);

        service.replace("ord-1", replaced);
        OrderResponse cached = service.getById("ord-1");

        assertEquals("new@example.com", cached.customerEmail());
        verify(repository, never()).findById(any());
    }

    @Test
    void writeThrough_failedPatchDoesNotTouchCache() {
        when(repository.findById("ord-1")).thenReturn(Optional.of(order(OrderStatus.CREATED, 0L)));
        when(updateUseCase.execute(any(), any(), any()))
                .thenThrow(new co.com.pragma.orderengine.domain.model.exception.VersionConflictException("stale"));

        service.getById("ord-1");
        assertThrows(co.com.pragma.orderengine.domain.model.exception.VersionConflictException.class,
                () -> service.updateStatus("ord-1", OrderStatus.CANCELLED, 99L));

        assertEquals("CREATED", service.getById("ord-1").status());
    }
}
