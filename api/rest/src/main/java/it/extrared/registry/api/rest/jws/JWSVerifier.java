package it.extrared.registry.api.rest.jws;

import static it.extrared.registry.utils.CommonUtils.debug;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import it.extrared.registry.api.rest.exceptions.JWSVerificationException;
import it.extrared.registry.utils.CommonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import org.jboss.logging.Logger;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;

/**
 * Verifies detached JWS (JSON Web Signature) tokens using the JOSE4J library. The verifier
 * reattaches the raw request-body payload to the compact serialisation, fetches the public keys
 * from the caller's JWKS endpoint, and checks the RS256 signature.
 */
@ApplicationScoped
public class JWSVerifier {

    @Inject Vertx vertx;

    private static final Logger LOGGER = Logger.getLogger(JWSVerifier.class);

    /**
     * Verifies that {@code detachedJws} is a valid detached compact JWS whose payload is {@code
     * rawBody}, signed with a key published at {@code jwksUri}.
     *
     * <p>The operation is executed on a worker thread via {@link Vertx#executeBlocking} to avoid
     * blocking the event loop during HTTP key-set retrieval.
     *
     * @param detachedJws the compact JWS string with an empty payload segment ({@code
     *     header..signature}).
     * @param rawBody the original request body bytes used as the JWS payload.
     * @param jwksUri the URL of the JWKS endpoint that exposes the signing public key.
     * @return a {@link Uni} that completes when the signature is valid, or fails with a {@link
     *     it.extrared.registry.api.rest.exceptions.JWSVerificationException} if verification fails
     *     for any reason.
     */
    public Uni<Void> verifyDetachedJws(String detachedJws, byte[] rawBody, String jwksUri) {
        CommonUtils.debug(LOGGER, () -> "Body is: %s".formatted(new String(rawBody)));
        Uni<Void> verify =
                Uni.createFrom().deferred(() -> verifyAsUni(detachedJws, rawBody, jwksUri));
        return vertx.executeBlocking(verify);
    }

    private Uni<Void> verifyAsUni(String detachedJws, byte[] rawBody, String jwksUri) {
        return Uni.createFrom()
                .voidItem()
                .invoke(v -> blockingVerifyDetachedJws(detachedJws, rawBody, jwksUri));
    }

    private void blockingVerifyDetachedJws(String detachedJws, byte[] rawBody, String jwksUri) {
        try {
            debug(
                    LOGGER,
                    () ->
                            "Verifying integrity of payload %s with jwks uri %s"
                                    .formatted(new String(rawBody), jwksUri));
            String[] parts = detachedJws.split("\\.");
            if (parts.length != 3 || !parts[1].isEmpty()) {
                throw new JWSVerificationException(
                        "Invalid detached JWS: expected header..signature format");
            }

            JsonWebSignature jws = new JsonWebSignature();
            String encodedPayload = Base64Url.encode(rawBody);
            String reattachedJws = parts[0] + "." + encodedPayload + "." + parts[2];

            jws.setCompactSerialization(reattachedJws);

            HttpsJwks httpsJwks = new HttpsJwks(jwksUri);
            HttpsJwksVerificationKeyResolver keyResolver =
                    new HttpsJwksVerificationKeyResolver(httpsJwks);

            jws.setKey(keyResolver.resolveKey(jws, Collections.emptyList()));
            if (!jws.verifySignature()) {
                throw new JWSVerificationException("Invalid signature");
            }

        } catch (Throwable e) {
            LOGGER.errorf(e, "JWS verification failed with exception: %s", e.getClass().getName());
            throw new JWSVerificationException("JWS processing error: " + e.getMessage());
        }
    }
}
