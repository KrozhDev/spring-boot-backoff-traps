package dev.backofftraps.traps.jackson;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SAFE. Tune Boot's mapper instead of replacing it: a customizer keeps {@code spring.jackson.*}
 * and all Boot defaults, and adds only what you need.
 */
@Configuration(proxyBeanMethods = false)
public class JacksonCustomizerConfig {

    @Bean
    Jackson2ObjectMapperBuilderCustomizer indentOutput() {
        return builder -> builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT);
    }
}
