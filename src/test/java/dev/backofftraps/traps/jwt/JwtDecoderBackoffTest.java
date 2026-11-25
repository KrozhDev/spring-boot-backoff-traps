package dev.backofftraps.traps.jwt;

import dev.backofftraps.app.HelloController;
import dev.backofftraps.support.SecuredWeb;
import dev.backofftraps.support.Tokens;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * One {@code implements JwtDecoder} turns off signature validation for every incoming request.
 */
class JwtDecoderBackoffTest {

    private static final Tokens TRUSTED_ISSUER = Tokens.generate();
    private static final Tokens ATTACKER = Tokens.generate();

    @TempDir
    static Path keys;

    private static WebApplicationContextRunner runner;

    @BeforeAll
    static void resourceServerTrustsOneKey() {
        runner = SecuredWeb.runner().withPropertyValues(
                "spring.security.oauth2.resourceserver.jwt.public-key-location="
                        + TRUSTED_ISSUER.publicKeyLocation(keys));
    }

    @Test
    @DisplayName("Boot's decoder: a valid token gets 200, a forged one gets 401")
    void bootDecoderVerifiesSignature() {
        runner.run(context -> {
            assertThat(context).getBean(JwtDecoder.class).isInstanceOf(NimbusJwtDecoder.class);

            var mvc = SecuredWeb.mockMvc(context);
            mvc.perform(get(HelloController.PATH).header("Authorization", bearer(TRUSTED_ISSUER)))
                    .andExpect(status().isOk());
            mvc.perform(get(HelloController.PATH).header("Authorization", bearer(ATTACKER)))
                    .andExpect(status().isUnauthorized());
        });
    }

    @Test
    @DisplayName("TRAP: own JwtDecoder bean, Boot's decoder is skipped, a forged token gets 200")
    void ownJwtDecoderAcceptsForgedToken() {
        runner.withBean(TokenReaderTrap.class).run(context -> {
            assertThat(context).getBean(JwtDecoder.class).isInstanceOf(TokenReaderTrap.class);
            assertThat(context).doesNotHaveBean(NimbusJwtDecoder.class);

            SecuredWeb.mockMvc(context)
                    .perform(get(HelloController.PATH).header("Authorization", bearer(ATTACKER)))
                    .andExpect(status().isOk());
        });
    }

    @Test
    @DisplayName("SAFE: a plain claims reader, Boot's decoder stays, a forged token gets 401")
    void plainReaderKeepsVerification() {
        runner.withBean(TokenClaimsReader.class).run(context -> {
            assertThat(context).getBean(JwtDecoder.class).isInstanceOf(NimbusJwtDecoder.class);
            assertThat(context.getBean(TokenClaimsReader.class).read(TRUSTED_ISSUER.sign("svc")))
                    .containsEntry("sub", "svc");

            SecuredWeb.mockMvc(context)
                    .perform(get(HelloController.PATH).header("Authorization", bearer(ATTACKER)))
                    .andExpect(status().isUnauthorized());
        });
    }

    private static String bearer(Tokens issuer) {
        return "Bearer " + issuer.sign("client");
    }
}
