package dev.backofftraps.traps.kafka;

import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * SAFE. Customize Boot's factory instead of replacing it: {@code spring.kafka.*} stays applied,
 * the customizer adds only the extra setting.
 */
@Configuration(proxyBeanMethods = false)
public class ConsumerFactoryCustomizerConfig {

    public static final String CLIENT_ID = "orders-consumer";

    @Bean
    DefaultKafkaConsumerFactoryCustomizer clientId() {
        return factory -> factory.updateConfigs(Map.of("client.id", CLIENT_ID));
    }
}
