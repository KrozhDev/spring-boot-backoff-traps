package dev.backofftraps.traps.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.core.ConsumerFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A hand-made {@link ConsumerFactory} silently drops {@code spring.kafka.*}, including SASL.
 * No broker is needed: the tests read the factory configuration.
 */
class ConsumerFactoryBackoffTest {

    private static final String SECURITY_PROTOCOL = "security.protocol";
    private static final String SASL_SSL = "SASL_SSL";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KafkaAutoConfiguration.class))
            .withPropertyValues(
                    "spring.kafka.bootstrap-servers=broker:9093",
                    "spring.kafka.security.protocol=" + SASL_SSL,
                    "spring.kafka.consumer.group-id=orders");

    @Test
    @DisplayName("Boot's factory: spring.kafka.* is applied, including the security protocol")
    void bootFactoryAppliesProperties() {
        runner.run(context -> assertThat(configs(context.getBean(ConsumerFactory.class)))
                .containsEntry(SECURITY_PROTOCOL, SASL_SSL)
                .containsEntry("group.id", "orders"));
    }

    @Test
    @DisplayName("TRAP: own factory, security protocol and group id are silently gone")
    void ownFactoryDropsProperties() {
        runner.withUserConfiguration(OwnConsumerFactoryTrap.class).run(context ->
                assertThat(configs(context.getBean(ConsumerFactory.class)))
                        .doesNotContainKey(SECURITY_PROTOCOL)
                        .doesNotContainKey("group.id"));
    }

    @Test
    @DisplayName("SAFE: a customizer adds a setting and keeps spring.kafka.*")
    void customizerKeepsProperties() {
        runner.withUserConfiguration(ConsumerFactoryCustomizerConfig.class).run(context ->
                assertThat(configs(context.getBean(ConsumerFactory.class)))
                        .containsEntry(SECURITY_PROTOCOL, SASL_SSL)
                        .containsEntry("client.id", ConsumerFactoryCustomizerConfig.CLIENT_ID));
    }

    private static Map<String, Object> configs(ConsumerFactory<?, ?> factory) {
        return factory.getConfigurationProperties();
    }
}
