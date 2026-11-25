package dev.backofftraps.traps.jwt;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * TRAP. A helper that only needs to read claims of a token the service obtained itself,
 * so it never verifies the signature.
 * <p>
 * Because it implements {@link JwtDecoder} and is a bean, Spring Boot's
 * {@code @ConditionalOnMissingBean(JwtDecoder.class)} skips the verifying decoder, and the
 * resource server validates every incoming request with this class: no signature, no expiry check.
 */
public class TokenReaderTrap implements JwtDecoder {

    @Override
    public Jwt decode(String token) {
        try {
            JWT parsed = JWTParser.parse(token);
            var claims = parsed.getJWTClaimsSet();
            return new Jwt(token,
                    toInstant(claims.getIssueTime()),
                    toInstant(claims.getExpirationTime()),
                    parsed.getHeader().toJSONObject(),
                    claims.getClaims());
        } catch (ParseException e) {
            throw new JwtException("Malformed token", e);
        }
    }

    private static Instant toInstant(Date date) {
        return Optional.ofNullable(date).map(Date::toInstant).orElse(null);
    }
}
