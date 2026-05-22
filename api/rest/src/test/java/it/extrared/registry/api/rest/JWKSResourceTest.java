package it.extrared.registry.api.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.List;
import org.jboss.logging.Logger;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class JWKSResourceTest {

    private static final Logger LOGGER = Logger.getLogger(JWKSResourceTest.class);

    @Test
    public void testJWKSEndpoint() throws Exception {
        String jwks =
                given().when()
                        .get("/.well-known/jwks.json") // corretto
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .extract()
                        .body()
                        .asString();

        JsonWebKeySet jwkSet = new JsonWebKeySet(jwks);
        List<JsonWebKey> keys = jwkSet.getJsonWebKeys();

        assertEquals(1, keys.size());

        JsonWebKey jwk = keys.getFirst();

        assertEquals("RSA", jwk.getKeyType());
        assertEquals("sig", jwk.getUse());
        assertNotNull(jwk.getKeyId());

        RsaJsonWebKey rsaJwk = (RsaJsonWebKey) jwk;
        assertNotNull(rsaJwk.getRsaPublicKey());
        assertNotNull(rsaJwk.getRsaPublicKey().getModulus());
        assertNotNull(rsaJwk.getRsaPublicKey().getPublicExponent());
        assertEquals(AlgorithmIdentifiers.RSA_USING_SHA256, jwk.getAlgorithm());
    }
}
