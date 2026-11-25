package dev.backofftraps.traps.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A local {@link CacheManager} "for one task" becomes the cache of the whole application.
 * No Redis server is needed: the tests check which manager is wired, not cache traffic.
 */
class CacheManagerBackoffTest {

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    static class CachingOn {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class, CacheAutoConfiguration.class))
            .withUserConfiguration(CachingOn.class)
            .withPropertyValues("spring.cache.type=redis");

    @Test
    @DisplayName("Boot's cache: spring.cache.type=redis gives a Redis cache manager")
    void bootCreatesRedisCacheManager() {
        runner.run(context ->
                assertThat(context).getBean(CacheManager.class).isInstanceOf(RedisCacheManager.class));
    }

    @Test
    @DisplayName("TRAP: own local cache manager, Redis is gone, every pod caches on its own")
    void localCacheManagerReplacesRedis() {
        runner.withUserConfiguration(LocalCacheManagerTrap.class).run(context -> {
            assertThat(context).getBean(CacheManager.class).isInstanceOf(ConcurrentMapCacheManager.class);
            assertThat(context).doesNotHaveBean(RedisCacheManager.class);
        });
    }

    @Test
    @DisplayName("SAFE: defaultCandidate = false, the local manager exists by name, Redis stays the default")
    void nonDefaultLocalManagerKeepsRedis() {
        runner.withUserConfiguration(NonDefaultLocalCacheManagerConfig.class).run(context -> {
            // AssertJ's getBean(type) demands a single bean of the type and counts non-default ones too;
            // Spring's own getBean(type) resolves the default candidate, exactly like injection does.
            assertThat(context.getBeanNamesForType(CacheManager.class)).hasSize(2);
            assertThat(context.getBean(CacheManager.class)).isInstanceOf(RedisCacheManager.class);
            assertThat(context.getBean("localCacheManager")).isInstanceOf(ConcurrentMapCacheManager.class);
        });
    }
}
