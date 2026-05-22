package it.extrared.registry.api.rest;

import io.smallrye.jwt.util.KeyUtils;
import io.smallrye.jwt.util.ResourceUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import it.extrared.registry.MetadataRegistryConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;

@ApplicationScoped
public class JWKSResourceImpl implements it.extrared.registry.api.rest.JWKSResource {

    @ConfigProperty(name = "smallrye.jwt.encrypt.key.location")
    String publicKeyLocation;

    @Inject MetadataRegistryConfig config;

    @Inject Vertx vertx;
    private static final Logger LOGGER = Logger.getLogger(JWKSResourceImpl.class);

    @Override
    public Uni<String> jwks() throws Exception {
        Uni<RsaJsonWebKey> jwk = load().map(this::asRsaJsonWebKey);
        return jwk.map(k -> new JsonWebKeySet(k).toJson());
    }

    private RsaJsonWebKey asRsaJsonWebKey(PublicKey pk) {
        RsaJsonWebKey jwk = new RsaJsonWebKey((RSAPublicKey) pk);
        jwk.setKeyId(config.keyId());
        jwk.setUse("sig");
        jwk.setAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA256);
        return jwk;
    }

    private Uni<PublicKey> load() {
        Uni<PublicKey> pk = Uni.createFrom().deferred(() -> Uni.createFrom().item(loadBlocking()));
        return vertx.executeBlocking(pk);
    }

    private PublicKey loadBlocking() {
        try {
            return KeyUtils.decodePublicKey(ResourceUtils.readResource(publicKeyLocation));
        } catch (Exception e) {
            LOGGER.error("Error while reading public key for JWKS generation", e);
            throw new RuntimeException(e);
        }
    }
}
