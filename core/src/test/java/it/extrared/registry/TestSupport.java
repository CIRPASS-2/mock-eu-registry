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
package it.extrared.registry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import io.quarkus.test.InjectMock;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import it.extrared.registry.dpp.DPPFetcher;
import it.extrared.registry.dpp.validation.ValidationReport;
import it.extrared.registry.dpp.validation.ValidationRestClient;
import java.util.Objects;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class TestSupport {
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
}
