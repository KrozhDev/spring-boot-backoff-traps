package dev.backofftraps.traps.security;

import dev.backofftraps.app.HelloController;
import dev.backofftraps.support.SecuredWeb;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A chain for one path switches off Boot's default "authenticate everything" chain.
 */
class SecurityFilterChainBackoffTest {

    @Test
    @DisplayName("Boot's default chain: an anonymous request is not let through")
    void defaultChainProtectsEverything() {
        SecuredWeb.runner().run(context -> {
            var status = SecuredWeb.mockMvc(context)
                    .perform(get(HelloController.PATH))
                    .andReturn().getResponse().getStatus();

            assertThat(status).as("401 or a redirect to the login page").isIn(401, 302);
        });
    }

    @Test
    @DisplayName("TRAP: a chain only for /actuator, every other path is open to anonymous requests")
    void actuatorOnlyChainOpensTheRest() {
        SecuredWeb.runner().withUserConfiguration(ActuatorOnlySecurityTrap.class).run(context ->
                SecuredWeb.mockMvc(context)
                        .perform(get(HelloController.PATH))
                        .andExpect(status().isOk()));
    }

    @Test
    @DisplayName("SAFE: an explicit catch-all chain keeps every other path protected")
    void explicitCatchAllKeepsProtection() {
        SecuredWeb.runner().withUserConfiguration(ExplicitCatchAllSecurityConfig.class).run(context ->
                SecuredWeb.mockMvc(context)
                        .perform(get(HelloController.PATH))
                        .andExpect(status().isUnauthorized()));
    }
}
