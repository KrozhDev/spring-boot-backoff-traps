package dev.backofftraps.traps.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TRAP. A chain written "just for /actuator".
 * <p>
 * Any {@link SecurityFilterChain} bean switches off Boot's default chain ("every request must be
 * authenticated"). Requests outside {@code /actuator/**} match no chain at all and pass
 * without any security filter.
 */
@Configuration(proxyBeanMethods = false)
public class ActuatorOnlySecurityTrap {

    @Bean
    SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        return http.securityMatcher("/actuator/**")
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
