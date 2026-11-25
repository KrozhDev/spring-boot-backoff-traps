package dev.backofftraps.traps.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TRAP. An in-memory cache "for one small task".
 * <p>
 * Boot creates the Redis cache manager from {@code spring.cache.*} only while no
 * {@link CacheManager} bean exists. This bean becomes the cache manager of the whole application:
 * every {@code @Cacheable} turns local, and each pod keeps its own copy of the data.
 */
@Configuration(proxyBeanMethods = false)
public class LocalCacheManagerTrap {

    @Bean
    CacheManager localCacheManager() {
        return new ConcurrentMapCacheManager("one-task");
    }
}
