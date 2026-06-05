package it.extrared.registry.api.rest;

import static org.mockito.ArgumentMatchers.eq;

import io.quarkus.test.InjectMock;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import it.extrared.registry.dpp.DPPFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

public class TestSupport {

    @InjectMock DPPFetcher dppFetcher;

    protected static final String MOCK_DPP =
            """
                {
                  "id":1,
                  "productName":"just a mock"
                }
                """;

    protected static final String MOCK_DPP_2 =
            """
                {
                  "id":2,
                  "productName":"just a mock 2"
                }
                """;

    @BeforeEach
    public void beforeEach() {
        Mockito.doReturn(Uni.createFrom().item(mockResponse(MOCK_DPP)))
                .when(dppFetcher)
                .fetchDPP(eq("http://localhost:1111/dpp"));
        Mockito.doReturn(Uni.createFrom().item(mockResponse(MOCK_DPP_2)))
                .when(dppFetcher)
                .fetchDPP(eq("http://localhost:2222/dpp"));
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
