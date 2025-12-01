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
package it.extrared.registry.api.rest.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import it.extrared.registry.metadata.DPPMetadataEntry;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/metadata/v1")
public interface DPPMetadataResource {

    @Operation(
            summary = "Add DPP metadata",
            description =
                    """
                    Add or updates a DPP metadata entry to the registry.
                    The payload is validated against the configured json schema before being persisted it.
                    The response always includes the added/updated metadata plus the registry id associated to the them.
                    """)
    @Parameter(
            name = "autocompleteBy",
            description =
                    """
                    A list of metadata fields to be used to search in previously added metadata. Such retrieved metadata are
                    then used to autofill fields that are null or absent from the incoming payload as configured by
                    the configuration property registry.autocompletion-enabled-for. This is useful for EO that already provided
                    data to the registry to avoid continuing providing full metadata payload in subsequent request, if some properties
                    are constant between different product. As an example, assuming that it has been configured
                    registry.autocompletion-enabled-for=commodityCode,dataCarrierTypes,facilitiesId populating providing the parameter as /registry?autocompleteBy=reoId
                    will cause the commodityCode,dataCarrierTypes and facilitiesId properties to be automatically filled in the provided payload
                    equal to the corresponding ones in the first found metadata with same reoId.
                    """,
            in = ParameterIn.PATH)
    @POST
    Uni<RestResponse<DPPMetadataEntry>> addDPPMetadata(
            @RestQuery List<String> autocompleteBy, JsonNode jsonNode);
}
