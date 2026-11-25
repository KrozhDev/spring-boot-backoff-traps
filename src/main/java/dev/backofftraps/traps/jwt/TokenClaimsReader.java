package dev.backofftraps.traps.jwt;

import com.nimbusds.jwt.JWTParser;

import java.text.ParseException;
import java.util.Map;

/**
 * SAFE. The same job as {@link TokenReaderTrap}, but not a {@code JwtDecoder}: a plain class
 * cannot be mistaken for the decoder that guards incoming requests.
 * <p>
 * Reads claims without verification, so use it only for tokens the service obtained itself.
 */
public class TokenClaimsReader {

    public Map<String, Object> read(String token) {
        try {
            return JWTParser.parse(token).getJWTClaimsSet().getClaims();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Malformed token", e);
        }
    }
}
