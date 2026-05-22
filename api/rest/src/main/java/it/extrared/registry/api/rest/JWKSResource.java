package it.extrared.registry.api.rest;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/.well-known")
public interface JWKSResource {

    @GET
    @Path("/jwks.json")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<String> jwks() throws Exception;
}
