package dev.backofftraps.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * An RSA key pair that signs test tokens. A token signed by a different {@code Tokens} instance
 * is a forged token for this one.
 */
public final class Tokens {

    private final RSAKey key;

    private Tokens(RSAKey key) {
        this.key = key;
    }

    public static Tokens generate() {
        try {
            return new Tokens(new RSAKeyGenerator(2048).generate());
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Writes the public key as PEM and returns a value for
     * {@code spring.security.oauth2.resourceserver.jwt.public-key-location}.
     */
    public String publicKeyLocation(Path dir) {
        try {
            var encoded = Base64.getMimeEncoder(64, "\n".getBytes())
                    .encodeToString(key.toRSAPublicKey().getEncoded());
            var pem = "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----\n";
            var file = Files.writeString(dir.resolve("public.pem"), pem);
            return file.toUri().toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    public String sign(String subject) {
        var now = Instant.now();
        var claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(Duration.ofHours(1))))
                .build();
        try {
            var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }
}
