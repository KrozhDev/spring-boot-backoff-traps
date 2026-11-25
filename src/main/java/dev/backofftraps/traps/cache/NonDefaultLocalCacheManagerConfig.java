package dev.backofftraps.traps.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SAFE. {@code defaultCandidate = false} (Spring 6.2): the bean exists and is used by name,
 * {@code @Cacheable(cacheManager = "localCacheManager")}, but {@code @ConditionalOnMissingBean}
 * ignores it, so Boot still creates the Redis cache manager for everything else.
 */
@Configuration(proxyBeanMethods = false)
public class NonDefaultLocalCacheManagerConfig {

    @Bean(defaultCandidate = false)
    CacheManager localCacheManager() {
        return new ConcurrentMapCacheManager("one-task");
    }
}
