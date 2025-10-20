package it.extrared.registry.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.utils.MultiMap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Map roles retrieved from the security identity to the api internal roles, namely one of */
@ApplicationScoped
public class RoleMapper implements SecurityIdentityAugmentor {

    @Inject MetadataRegistryConfig config;

    @Override
    public int priority() {
        return SecurityIdentityAugmentor.super.priority();
    }

    @Override
    public Uni<SecurityIdentity> augment(
            SecurityIdentity securityIdentity,
            AuthenticationRequestContext authenticationRequestContext) {
        return Uni.createFrom().item(copyAndAugment(securityIdentity));
    }

    @Override
    public Uni<SecurityIdentity> augment(
            SecurityIdentity identity,
            AuthenticationRequestContext context,
            Map<String, Object> attributes) {
        return SecurityIdentityAugmentor.super.augment(identity, context, attributes);
    }

    protected Supplier<SecurityIdentity> copyAndAugment(SecurityIdentity identity) {
        if (identity.isAnonymous()) {
            return () -> identity;
        } else {
            QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
            addInternalRoles(identity, builder);
            return builder::build;
        }
    }

    private void addInternalRoles(
            SecurityIdentity identity, QuarkusSecurityIdentity.Builder builder) {
        Set<String> roles = identity.getRoles();
        MultiMap<String, String> mappings = config.rolesMappings();
        if (roles != null && !roles.isEmpty()) {
            for (String r : roles) {

                List<String> internalRoles = mappings.get(r);
                if (internalRoles != null)
                    builder.addRoles(
                            internalRoles.stream()
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toSet()));
            }
        }
    }
}
