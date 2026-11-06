package co.com.pragma.orderengine.infrastructure.driven_adapters.cache;

import co.com.pragma.orderengine.domain.ports.out.IIdempotencyKeyLockPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisIdempotencyKeyLockAdapter implements IIdempotencyKeyLockPort {

    private static final Logger log = LoggerFactory.getLogger(RedisIdempotencyKeyLockAdapter.class);

    static final String KEY_PREFIX = "idempotency:";
    static final String MARKER = "PROCESSED";
    static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotencyKeyLockAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryAcquire(String key) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + key, MARKER, TTL);
        boolean result = Boolean.TRUE.equals(acquired);
        log.debug("Idempotency key '{}' {}", key, result ? "acquired (new, TTL 24h)" : "already exists");
        return result;
    }

    @Override
    public void release(String key) {
        redisTemplate.delete(KEY_PREFIX + key);
        log.debug("Idempotency key '{}' released", key);
    }
}
