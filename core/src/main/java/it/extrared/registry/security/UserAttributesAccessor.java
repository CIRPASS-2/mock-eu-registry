package it.extrared.registry.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

/** Provides access to user attributes, aka token claims. */
@ApplicationScoped
public class UserAttributesAccessor {

    @Inject JsonWebToken identity;

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
