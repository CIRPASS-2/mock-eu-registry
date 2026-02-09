package it.extrared.registry.api.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import it.extrared.registry.dpp.validation.ValidatedMetadataEntry;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(DPPMetadataResourceDataValidationTest.DPPValidationProfile.class)
public class DPPMetadataResourceDataValidationTest {

    private static final String METADATA_1 =
            """
            {
                "reoId":"12345",
                "upi":"12345",
                "commodityCode":"122267310",
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"]
              }
            """;

    @Test
    public void testAddDppMetadataWithDPPValidation() {
        ValidatedMetadataEntry metadata =
                given().when()
                        .body(METADATA_1)
                        .contentType(ContentType.JSON)
                        .post("/metadata/v1")
                        .then()
                        .statusCode(201)
                        .extract()
                        .body()
                        .as(ValidatedMetadataEntry.class);
        assertNotNull(metadata);
        assertNotNull(metadata.getRegistryId());
        assertTrue(metadata.getValidation().isValid());
    }

    public static class DPPValidationProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("registry.dpp-validation-enabled", "true");
        }
    }
}
