package it.extrared.registry.api.rest;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/.well-known")
public interface JWKSResource {

    @GET
    @Path("/jwks.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Retrieve the registry JWKS",
            description =
                    """
            Returns the JSON Web Key Set (JWKS) containing the registry's RSA public key \
            (RS256). Clients use this endpoint to obtain the public key needed to verify \
            Proof of Registration JWTs issued by this registry. \
            No authentication is required.
            """)
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        description = "JWKS document containing the registry's RSA public key."),
                @APIResponse(
                        responseCode = "500",
                        description = "The public key could not be loaded.")
            })
    Uni<String> jwks() throws Exception;
}
