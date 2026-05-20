package it.extrared.registry.api.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.config.SmallRyeConfig;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.security.UserAttributesAccessor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@ConnectWireMock
public class DppMetadataResourceJWSTest extends TestSupport {

    private static KeyPair testKeyPair;
    private static final String KEY_ID = "test-key-1";
    private static final String JWKS_PATH = "/.well-known/jwks.json";

    @InjectMock UserAttributesAccessor attributesAccessor;

    @Inject SmallRyeConfig smallRyeConfig;

    @Produces
    @ApplicationScoped
    @Mock
    MetadataRegistryConfig registryConfig() {
        return smallRyeConfig.getConfigMapping(MetadataRegistryConfig.class);
    }

    @InjectMock MetadataRegistryConfig config;

    WireMock wiremock;

    @ConfigProperty(name = "quarkus.wiremock.devservices.port")
    Integer wireMockPort;

    private static final String METADATA_1 =
            """
            {
                "reoId":"123wdf433sd",
                "upi":"1re335",
                "commodityCode":"122267310",
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"],
                "liveURL":"http://localhost:2222/dpp"
            }
            """;

    @BeforeAll
    static void generateKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        testKeyPair = gen.generateKeyPair();
    }

    @BeforeEach
    public void setup() throws Exception {
        MetadataRegistryConfig.Jws jws = Mockito.mock(MetadataRegistryConfig.Jws.class);
        when(config.jws()).thenReturn(jws);
        when(jws.verificationEnabled()).thenReturn(true);
        when(config.defaultTemplateName()).thenReturn("rest-default-schema.json");
        when(jws.jwksUriClaimName()).thenReturn("jwksUri");
        when(jws.headerName()).thenReturn("x-jws-signature");
        when(config.upiFieldName()).thenReturn("upi");
        when(config.reoidFieldName()).thenReturn("reoId");
        when(config.reoidFromClaimEnabled()).thenReturn(false);
        when(config.liveUrlFieldName()).thenReturn("liveURL");
        when(config.dppValidationEnabled()).thenReturn(false);
        wiremock.resetMappings();

        wiremock.register(
                get(urlEqualTo(JWKS_PATH))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(buildJwks(testKeyPair.getPublic()))));
        String jwksUri = "http://localhost:" + wireMockPort + JWKS_PATH;
        when(attributesAccessor.getClaim("jwksUri")).thenReturn(jwksUri);
    }

    private String buildJwks(PublicKey publicKey) throws Exception {
        RsaJsonWebKey jwk = new RsaJsonWebKey((RSAPublicKey) publicKey);
        jwk.setKeyId(KEY_ID);
        jwk.setUse("sig");
        jwk.setAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA256);
        return new JsonWebKeySet(jwk).toJson();
    }

    @Test
    public void testVerification() throws Exception {
        byte[] bodyBytes = METADATA_1.trim().getBytes(StandardCharsets.UTF_8);
        System.out.println("Length: " + bodyBytes.length);
        System.out.println("First byte: " + bodyBytes[0]); // deve essere 123 = {
        System.out.println("Last byte: " + bodyBytes[bodyBytes.length - 1]); // deve essere 125 = }
        System.out.println(Arrays.toString(bodyBytes));
        given().contentType(ContentType.JSON)
                .header("x-jws-signature", generateDetachedJws(bodyBytes))
                .body(bodyBytes)
                .when()
                .post("/metadata/v1/registerDPP")
                .then()
                .statusCode(201);
    }

    @Test
    void testRejectTamperedBody() throws Exception {
        String tamperedBody = METADATA_1.replace("1re335", "tamperedUPI");
        byte[] bodyBytes = tamperedBody.trim().getBytes(StandardCharsets.UTF_8);

        given().contentType(ContentType.JSON)
                .header(
                        "x-jws-signature",
                        generateDetachedJws(METADATA_1.trim().getBytes(StandardCharsets.UTF_8)))
                .body(bodyBytes)
                .when()
                .post("/metadata/v1/registerDPP")
                .then()
                .statusCode(401);
    }

    private String generateDetachedJws(byte[] body) throws Exception {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayloadBytes(body);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setKey(testKeyPair.getPrivate());
        jws.setKeyIdHeaderValue(KEY_ID);
        String compact = jws.getCompactSerialization();
        String[] parts = compact.split("\\.");
        return parts[0] + ".." + parts[2];
    }
}
