package co.com.pragma.orderengine.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RedisCacheConfigTest {

    private final RedisCacheConfig config = new RedisCacheConfig();

    @Test
    void defaultTtlShouldBeOneHour() {
        RedisCacheConfiguration cfg = config.defaultCacheConfiguration();

        assertEquals(Duration.ofHours(1), cfg.getTtlFunction().getTimeToLive("any", null));
        assertFalse(cfg.getAllowCacheNullValues());
    }

    @Test
    void cacheManagerShouldApplyGlobalTtlToEveryCache() {
        RedisConnectionFactory factory = Mockito.mock(RedisConnectionFactory.class);
        RedisCacheManager manager = config.cacheManager(factory, config.defaultCacheConfiguration());
        manager.afterPropertiesSet();

        RedisCache cache = assertInstanceOf(RedisCache.class, manager.getCache("orders"));
        assertNotNull(cache);
        assertEquals(Duration.ofHours(1),
                cache.getCacheConfiguration().getTtlFunction().getTimeToLive("k", null));
    }
}
