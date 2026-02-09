package it.extrared.registry.dpp;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/** Class providing functionality to retrieve a DPP from the decentralized repository. */
@ApplicationScoped
public class DPPFetcher {

    @Inject WebClient webClient;

    /**
     * Given an url it issues an http request with supported mime types and returns the payload
     *
     * @param url the url to invoke.
     * @return the response.
     */
    public Uni<HttpResponse<Buffer>> fetchDPP(String url) {
        List<String> mimes = List.of("application/json", "application/ld+json");
        HttpRequest<Buffer> request = webClient.getAbs(url);
        request.headers().add("Accept", String.join(", ", mimes));
        return request.send();
    }
}
