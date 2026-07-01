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

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import it.extrared.registry.api.rest.exceptions.ErrorPayload;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.security.UserAttributesAccessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(ReoIdFromClaimPropertyProfile.class)
public class DppMetadataResourceReoIdFromClaimTest extends TestSupport {

    private static final String METADATA_1 =
            """
            {
                "upi":"12345",
                "commodityCode":"122267310",
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"],
                "liveURL":"http://localhost:1111/dpp"
              }
            """;

    private static final String METADATA_UPD =
            """
            {
                "upi":"12345",
                "commodityCode":"233367221",
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"],
                "liveURL":"http://localhost:1111/dpp"
              }
            """;

    @InjectMock UserAttributesAccessor attributesAccessor;

    @Test
    public void testPreventUpdatesOnNotOwnedDPP() {
        Mockito.when(attributesAccessor.getReoId()).thenReturn("111111").thenReturn("222222");
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
        ErrorPayload payload =
                given().when()
                        .body(METADATA_UPD)
                        .contentType(ContentType.JSON)
                        .post("/metadata/v1")
                        .then()
                        .statusCode(400)
                        .extract()
                        .body()
                        .as(ErrorPayload.class);
        assertTrue(payload.getMessage().contains("cannot be updated by '222222'"));
    }
}
