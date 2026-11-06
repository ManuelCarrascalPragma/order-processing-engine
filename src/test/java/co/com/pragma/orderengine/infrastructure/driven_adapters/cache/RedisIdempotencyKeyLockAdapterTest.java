package co.com.pragma.orderengine.infrastructure.driven_adapters.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisIdempotencyKeyLockAdapterTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private RedisIdempotencyKeyLockAdapter adapter;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        redisTemplate = Mockito.mock(StringRedisTemplate.class);
        valueOps = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        adapter = new RedisIdempotencyKeyLockAdapter(redisTemplate);
    }

    @Test
    void shouldAcquireNewKeyAtomicallyWith24HourTtl() {
        when(valueOps.setIfAbsent("idempotency:k1", "PROCESSED", Duration.ofHours(24))).thenReturn(true);

        assertTrue(adapter.tryAcquire("k1"));
        verify(valueOps).setIfAbsent("idempotency:k1", "PROCESSED", Duration.ofHours(24));
    }

    @Test
    void shouldRejectKeyThatAlreadyExists() {
        when(valueOps.setIfAbsent("idempotency:k1", "PROCESSED", Duration.ofHours(24))).thenReturn(false);

        assertFalse(adapter.tryAcquire("k1"));
    }

    @Test
    void shouldTreatNullResultAsNotAcquired() {
        when(valueOps.setIfAbsent("idempotency:k1", "PROCESSED", Duration.ofHours(24))).thenReturn(null);

        assertFalse(adapter.tryAcquire("k1"));
    }

    @Test
    void shouldReleaseKey() {
        adapter.release("k1");

        verify(redisTemplate).delete("idempotency:k1");
    }
}
