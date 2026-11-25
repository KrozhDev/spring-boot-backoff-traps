package dev.backofftraps.traps.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TRAP. "We just need an ObjectMapper bean somewhere."
 * <p>
 * Spring Boot creates its own mapper with {@code @ConditionalOnMissingBean}, so this one replaces
 * it for the whole application: {@code spring.jackson.*} and Boot's defaults are gone, and an
 * unknown JSON field now fails deserialization (a 400 for every client sending a new field).
 */
@Configuration(proxyBeanMethods = false)
public class PlainObjectMapperTrap {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
