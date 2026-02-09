package it.extrared.registry;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class Producers {

    @Produces
    @ApplicationScoped
    public WebClient webClient(Vertx vertx) {
        return WebClient.create(vertx);
    }
}
