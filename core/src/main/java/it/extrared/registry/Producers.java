package it.extrared.registry;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/** Class for produced beans. */
@ApplicationScoped
public class Producers {

    /**
     * Produces a vertx web client
     *
     * @param vertx the vertx instance
     * @return the WebClient instance.
     */
    @Produces
    @ApplicationScoped
    public WebClient webClient(Vertx vertx) {
        return WebClient.create(vertx);
    }
}
