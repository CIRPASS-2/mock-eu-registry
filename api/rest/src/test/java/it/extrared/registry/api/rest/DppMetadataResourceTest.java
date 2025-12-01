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
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import it.extrared.registry.metadata.DPPMetadataEntry;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DppMetadataResourceTest {

    private static final String METADATA_1 =
            """
            {
                "reoId":"12345",
                "upi":"12345",
                "commodityCode":"122267310",
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"]
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
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"]
              }
            """;

    private static final String METADATA_3 =
            """
            {
                "reoId":"912345",
                "upi":"99999"
              }
            """;

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
}
