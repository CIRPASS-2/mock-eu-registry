package it.extrared.registry.api.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import it.extrared.registry.dpp.DPPFetcher;
import it.extrared.registry.dpp.validation.ValidatedMetadataEntry;
import it.extrared.registry.dpp.validation.ValidationReport;
import it.extrared.registry.dpp.validation.ValidationRestClient;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(DPPMetadataResourceDataValidationTest.DPPValidationProfile.class)
public class DPPMetadataResourceDataValidationTest {

    @InjectMock @RestClient ValidationRestClient validationRestClient;

    @InjectMock DPPFetcher dppFetcher;

    @BeforeEach
    public void beforeEach() {
        String mockDpp =
                """
                {
                  "id":1,
                  "productName":"just a mock"
                }
                """;
        String mockDpp2 =
                """
                {
                  "id":2,
                  "productName":"just a mock 2"
                }
                """;
        ValidationReport validationReport = new ValidationReport();
        validationReport.setValid(true);
        validationReport.setValidatedWith("validated with mocks");
        ValidationReport invalidReport = new ValidationReport();
        invalidReport.setValid(false);
        invalidReport.setValidatedWith("validated with mocks");
        Mockito.doReturn(Uni.createFrom().item(mockResponse(mockDpp)))
                .when(dppFetcher)
                .fetchDPP(eq("localhost:1111/dpp"));
        Mockito.doReturn(Uni.createFrom().item(mockResponse(mockDpp2)))
                .when(dppFetcher)
                .fetchDPP(eq("localhost:2222/dpp"));
        Mockito.doReturn(Uni.createFrom().item(validationReport))
                .when(validationRestClient)
                .validate(
                        ArgumentMatchers.argThat(b -> Objects.equals(new String(b), mockDpp)),
                        any());
        Mockito.doReturn(Uni.createFrom().item(invalidReport))
                .when(validationRestClient)
                .validate(
                        ArgumentMatchers.argThat(b -> Objects.equals(new String(b), mockDpp2)),
                        any());
    }

    private HttpResponse<Buffer> mockResponse(String body) {
        HttpResponse<Buffer> response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.bodyAsBuffer()).thenReturn(Buffer.buffer(body));
        MultiMap headers =
                MultiMap.caseInsensitiveMultiMap().add("Content-Type", "application/json");
        Mockito.when(response.headers()).thenReturn(headers);
        Mockito.when(response.statusCode()).thenReturn(200);
        return response;
    }

    private static final String METADATA_1 =
            """
            {
                "reoId":"12345",
                "upi":"12345",
                "commodityCode":"122267310",
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"],
                "liveURL":"localhost:1111/dpp"
              }
            """;

    private static final String METADATA_2 =
            """
            {
                "reoId":"12345",
                "upi":"12345",
                "commodityCode":"122267310",
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"],
                "liveURL":"localhost:2222/dpp"
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
