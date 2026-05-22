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

    public String getReoId() {
        return identity.getClaim(registryConfig.reoidClaimName());
    }

    public String getReoName() {
        return identity.getClaim(registryConfig.reoNameClaimName());
    }

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
