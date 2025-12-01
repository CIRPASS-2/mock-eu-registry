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
package it.extrared.registry.api.rest.schema;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/schema/v1")
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
