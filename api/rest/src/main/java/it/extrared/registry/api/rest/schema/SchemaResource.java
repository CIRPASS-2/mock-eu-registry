package it.extrared.registry.api.rest.schema;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/schema")
public interface SchemaResource {

    @POST
    @Operation(
            summary = "Add a DPP metadata JSON schema",
            description =
                    "Add a DPP metadata JSON schema. If a schema already exists the newly added one becomes the current schema in use to validate and manage the DPP metadata.")
    Uni<RestResponse<Void>> addSchema(JsonNode node);

    @GET
    @Path("/current")
    @Operation(
            summary = "Retrieve the current DPP metadata JSON schema",
            description =
                    "Retrieve the DPP metadata JSON schema currently in use by the API,i.e. the last added.")
    Uni<RestResponse<JsonNode>> getCurrent();

    @DELETE
    @Path("/current")
    @Operation(
            summary = "Remove the current DPP metadata JSON schema",
            description =
                    "Remove the DPP metadata JSON schema currently in use by the API,i.e. the last added, causing the previous JSON schema, if any, to become the new current schema.")
    Uni<RestResponse<Void>> removeCurrent();
}
