package it.extrared.registry.api.rest;

import static io.restassured.RestAssured.given;
import static it.extrared.registry.api.rest.TestSupport.MOCK_DPP;
import static it.extrared.registry.api.rest.TestSupport.MOCK_DPP_2;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import it.extrared.registry.dpp.validation.ValidatedMetadataEntry;
import it.extrared.registry.dpp.validation.ValidationReport;
import it.extrared.registry.dpp.validation.ValidationRestClient;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(DPPMetadataResourceDataValidationTest.DPPValidationProfile.class)
public class DPPMetadataResourceDataValidationTest extends TestSupport {

    @InjectMock @RestClient ValidationRestClient validationRestClient;

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

    private void setupValidationMock() {
        ValidationReport validationReport = new ValidationReport();
        validationReport.setValid(true);
        validationReport.setValidatedWith("validated with mocks");
        ValidationReport invalidReport = new ValidationReport();
        invalidReport.setValid(false);
        invalidReport.setValidatedWith("validated with mocks");
        Mockito.doReturn(Uni.createFrom().item(validationReport))
                .when(validationRestClient)
                .validate(
                        ArgumentMatchers.argThat(b -> Objects.equals(new String(b), MOCK_DPP)),
                        any());
        Mockito.doReturn(Uni.createFrom().item(invalidReport))
                .when(validationRestClient)
                .validate(
                        ArgumentMatchers.argThat(b -> Objects.equals(new String(b), MOCK_DPP_2)),
                        any());
    }

    @Test
    public void testAddDppMetadataWithDPPValidation() {
        setupValidationMock();
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
