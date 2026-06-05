package it.extrared.registry.security;

import it.extrared.registry.MetadataRegistryConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

/** Provides access to user attributes, aka token claims. */
@ApplicationScoped
public class UserAttributesAccessor {

    @Inject JsonWebToken identity;

    @Inject MetadataRegistryConfig registryConfig;

    /**
     * Returns the Responsible Economic Operator identifier extracted from the current JWT, using
     * the claim name configured in {@link MetadataRegistryConfig#reoidClaimName()}.
     *
     * @return the reoId claim value, or {@code null} if the claim is absent.
     */
    public String getReoId() {
        return identity.getClaim(registryConfig.reoidClaimName());
    }

    /**
     * Returns the Responsible Economic Operator name extracted from the current JWT, using the
     * claim name configured in {@link MetadataRegistryConfig#reoNameClaimName()}.
     *
     * @return the reoName claim value, or {@code null} if the claim is absent.
     */
    public String getReoName() {
        return identity.getClaim(registryConfig.reoNameClaimName());
    }

    /**
     * Returns the JWKS URI extracted from the current JWT, using the claim name configured in
     * {@link MetadataRegistryConfig.Jws#jwksUriClaimName()}. This URI points to the public-key set
     * that can be used to verify a detached JWS produced by the caller.
     *
     * @return the jwksUri claim value, or {@code null} if the claim is absent.
     */
    public String getJwksUri() {
        return identity.getClaim(registryConfig.jws().jwksUriClaimName());
    }

    /**
     * Retrieve a claim value from the current jwt.
     *
     * @param attributeName the name of the claim.
     * @return the claim value if present.
     * @param <T> target claim type.
     */
    public <T> T getClaim(String attributeName) {
        return identity.getClaim(attributeName);
    }
}
