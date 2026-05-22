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

@ApplicationScoped
public class JWSService {

    @Inject UserAttributesAccessor userAttributesAccessor;

    @Inject JWSVerifier verifier;

    @Inject MetadataRegistryConfig config;

    private static final Logger LOGGER = Logger.getLogger(JWSService.class);

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
