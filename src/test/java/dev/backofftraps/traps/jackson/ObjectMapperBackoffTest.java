package dev.backofftraps.traps.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A plain {@code new ObjectMapper()} bean replaces Boot's mapper for the whole application.
 */
class ObjectMapperBackoffTest {

    record Order(String id) {
    }

    private static final String JSON_WITH_NEW_FIELD = "{\"id\":\"1\",\"comment\":\"field added by a client\"}";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withPropertyValues("spring.jackson.serialization.indent-output=true");

    @Test
    @DisplayName("Boot's mapper: unknown fields are ignored, spring.jackson.* is applied")
    void bootMapperIsTolerant() {
        runner.run(context -> {
            var mapper = context.getBean(ObjectMapper.class);

            assertThat(mapper.readValue(JSON_WITH_NEW_FIELD, Order.class).id()).isEqualTo("1");
            assertThat(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)).isTrue();
        });
    }

    @Test
    @DisplayName("TRAP: own ObjectMapper, a new field fails parsing, spring.jackson.* is ignored")
    void plainMapperBreaksClients() {
        runner.withUserConfiguration(PlainObjectMapperTrap.class).run(context -> {
            var mapper = context.getBean(ObjectMapper.class);

            assertThatThrownBy(() -> mapper.readValue(JSON_WITH_NEW_FIELD, Order.class))
                    .isInstanceOf(UnrecognizedPropertyException.class);
            assertThat(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)).isFalse();
        });
    }

    @Test
    @DisplayName("SAFE: a customizer tunes Boot's mapper and keeps its defaults")
    void customizerKeepsBootMapper() {
        runner.withUserConfiguration(JacksonCustomizerConfig.class).run(context -> {
            var mapper = context.getBean(ObjectMapper.class);

            assertThat(mapper.readValue(JSON_WITH_NEW_FIELD, Order.class).id()).isEqualTo("1");
            assertThat(context).hasBean("jacksonObjectMapper");
        });
    }
}
