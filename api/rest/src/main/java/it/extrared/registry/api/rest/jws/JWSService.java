package it.extrared.registry.api.rest.jws;

import static it.extrared.registry.utils.CommonUtils.debug;

import io.smallrye.mutiny.Uni;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.exceptions.InvalidOperationException;
import it.extrared.registry.security.UserAttributesAccessor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import java.util.Optional;
import org.jboss.logging.Logger;

/**
 * Orchestrates detached JWS (JSON Web Signature) verification for incoming metadata requests. When
 * verification is enabled via {@link MetadataRegistryConfig.Jws#verificationEnabled()}, the service
 * extracts the detached JWS from the configured HTTP header and the JWKS URI from the caller's JWT,
 * then delegates signature validation to {@link JWSVerifier}.
 */
@ApplicationScoped
public class JWSService {

    @Inject UserAttributesAccessor userAttributesAccessor;

    @Inject JWSVerifier verifier;

    @Inject MetadataRegistryConfig config;

    private static final Logger LOGGER = Logger.getLogger(JWSService.class);

    /**
     * Verifies the detached JWS signature of the request body, if JWS verification is enabled.
     *
     * <p>When enabled, the method:
     *
     * <ol>
     *   <li>Reads the detached JWS token from the HTTP header specified by {@link
     *       MetadataRegistryConfig.Jws#headerName()}.
     *   <li>Retrieves the caller's JWKS URI from the JWT claim specified by {@link
     *       MetadataRegistryConfig.Jws#jwksUriClaimName()}.
     *   <li>Delegates the actual signature check to {@link JWSVerifier#verifyDetachedJws}.
     * </ol>
     *
     * @param body the raw request body bytes whose integrity must be verified.
     * @param headers the HTTP request headers containing the detached JWS token.
     * @return a {@link Uni} that completes normally when verification succeeds, or fails with an
     *     {@link it.extrared.registry.api.rest.exceptions.JWSVerificationException} on invalid
     *     signature, or with an {@link it.extrared.registry.exceptions.InvalidOperationException}
     *     if the JWS header is missing.
     */
    public Uni<Void> verify(byte[] body, HttpHeaders headers) {
        if (!config.jws().verificationEnabled()) {
            debug(LOGGER, () -> "JWS verification is disabled");
            return Uni.createFrom().nullItem();
        }
        String detachedJws =
                getDetachedJwt(headers)
                        .orElseThrow(
                                () ->
                                        new InvalidOperationException(
                                                "Missing %s header"
                                                        .formatted(config.jws().headerName())));
        String jwksUri = userAttributesAccessor.getJwksUri();
        debug(
                LOGGER,
                () ->
                        "Retrieved jwks uri %s from token claim with name %s"
                                .formatted(jwksUri, config.jws().jwksUriClaimName()));
        debug(
                LOGGER,
                () ->
                        "Retrieving detached jws %s from header %s"
                                .formatted(detachedJws, config.jws().headerName()));

        return verifier.verifyDetachedJws(detachedJws, body, jwksUri);
    }

    private Optional<String> getDetachedJwt(HttpHeaders headers) {
        List<String> detached = headers.getRequestHeader(config.jws().headerName());
        String ret = null;
        if (detached != null && !detached.isEmpty()) {
            ret = detached.getFirst();
        }
        return Optional.ofNullable(ret);
    }
}
