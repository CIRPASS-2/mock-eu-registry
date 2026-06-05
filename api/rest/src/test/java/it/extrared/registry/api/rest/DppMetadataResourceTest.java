/*
 * Copyright 2024-2027 CIRPASS-2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.extrared.registry.api.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.security.UserAttributesAccessor;
import java.util.List;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class DppMetadataResourceTest extends TestSupport {

    private static final String METADATA_1 =
            """
            {
                "reoId":"12345",
                "upi":"12345",
                "commodityCode":"122267310",
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"],
                "liveURL":"http://localhost:1111/dpp"
              }
            """;

    private static final String METADATA_UPD =
            """
            {
                "reoId":"12345",
                "upi":"12345",
                "commodityCode":"233367221"
              }
            """;

    private static final String METADATA_2 =
            """
            {
                "reoId":"912345",
                "upi":"123456",
                "commodityCode":"122267310",
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"],
                "liveURL":"http://localhost:1111/dpp"
              }
            """;

    private static final String METADATA_3 =
            """
            {
                "reoId":"912345",
                "upi":"99999",
                "liveURL":"http://localhost:1111/dpp"
              }
            """;

    @InjectMock UserAttributesAccessor attributesAccessor;

    @Test
    public void testAddDppMetadataAndUpdate() {
        DPPMetadataEntry metadata =
                given().when()
                        .body(METADATA_1)
                        .contentType(ContentType.JSON)
                        .post("/metadata/v1")
                        .then()
                        .statusCode(201)
                        .extract()
                        .body()
                        .as(DPPMetadataEntry.class);
        assertNotNull(metadata);
        assertNotNull(metadata.getRegistryId());
        String registryId = metadata.getRegistryId();
        metadata =
                given().when()
                        .body(METADATA_UPD)
                        .contentType(ContentType.JSON)
                        .post("/metadata/v1")
                        .then()
                        .statusCode(201)
                        .extract()
                        .body()
                        .as(DPPMetadataEntry.class);
        assertEquals("233367221", metadata.getMetadata().get("commodityCode").asText());
        assertEquals(registryId, metadata.getRegistryId());
    }

    @Test
    public void testAutocomplete() {
        DPPMetadataEntry metadata =
                given().when()
                        .body(METADATA_2)
                        .contentType(ContentType.JSON)
                        .post("/metadata/v1")
                        .then()
                        .statusCode(201)
                        .extract()
                        .body()
                        .as(DPPMetadataEntry.class);
        assertNotNull(metadata);
        assertNotNull(metadata.getRegistryId());
        JsonNode carriers = metadata.getMetadata().get("dataCarrierTypes");
        metadata =
                given().when()
                        .request()
                        .queryParam("autocompleteBy", List.of("reoId"))
                        .body(METADATA_3)
                        .contentType(ContentType.JSON)
                        .post("/metadata/v1")
                        .then()
                        .statusCode(201)
                        .extract()
                        .body()
                        .as(DPPMetadataEntry.class);
        assertEquals("122267310", metadata.getMetadata().get("commodityCode").asText());
        assertEquals(carriers, metadata.getMetadata().get("dataCarrierTypes"));
    }

    @Test
    public void testProof() throws Exception {
        Mockito.when(attributesAccessor.getReoName()).thenReturn("testReo");
        DPPMetadataEntry metadata =
                given().when()
                        .body(METADATA_2)
                        .contentType(ContentType.JSON)
                        .post("/metadata/v1")
                        .then()
                        .statusCode(201)
                        .extract()
                        .body()
                        .as(DPPMetadataEntry.class);
        assertNotNull(metadata);
        assertNotNull(metadata.getRegistryId());
        String proof =
                given().when()
                        .param("reoId", "912345")
                        .request()
                        .get("/metadata/v1/%s/proof".formatted(metadata.getRegistryId()))
                        .then()
                        .statusCode(200)
                        .contentType("application/jwt")
                        .extract()
                        .body()
                        .asString();
        JwtClaims claims = parseAndVerifyJwt(proof);
        assertNotNull(claims.getClaimValue("dppHash"));
        assertNotNull(claims.getClaimValue("registryId"));
        assertNotNull(claims.getClaimValue("reoId"));
    }

    private JwtClaims parseAndVerifyJwt(String jwt) throws Exception {
        String jwks =
                given().when()
                        .get("/.well-known/jwks.json")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        JsonWebKeySet jwkSet = new JsonWebKeySet(jwks);
        JwksVerificationKeyResolver keyResolver =
                new JwksVerificationKeyResolver(jwkSet.getJsonWebKeys());

        JwtConsumer consumer =
                new JwtConsumerBuilder()
                        .setVerificationKeyResolver(keyResolver)
                        .setRequireExpirationTime()
                        .setExpectedIssuer("http://localhost:8080")
                        .build();

        return consumer.processToClaims(jwt);
    }
}
