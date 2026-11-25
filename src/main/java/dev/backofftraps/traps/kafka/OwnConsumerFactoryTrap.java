package dev.backofftraps.traps.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

/**
 * TRAP. A consumer factory built by hand from a few settings.
 * <p>
 * Boot builds its factory from {@code spring.kafka.*} only while no {@link ConsumerFactory} bean
 * exists. This one silently drops everything else configured there: security protocol, SASL,
 * group id, timeouts. Locally it works, against a secured cluster it does not connect.
 */
@Configuration(proxyBeanMethods = false)
public class OwnConsumerFactoryTrap {

    @Bean
    ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
    }
}
