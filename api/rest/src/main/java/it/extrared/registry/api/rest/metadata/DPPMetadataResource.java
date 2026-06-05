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

import io.smallrye.mutiny.Uni;
import it.extrared.registry.metadata.DPPMetadataEntry;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/metadata/v1")
public interface DPPMetadataResource {

    @Operation(
            summary = "Add DPP metadata",
            description =
                    """
                        Add or updates a DPP metadata entry to the registry.
                        The payload is validated against the configured json schema before being persisted.
                        The response always includes the added/updated metadata plus the registry id \
                        associated to them.
                        """)
    @Parameters({
        @Parameter(
                name = "autocompleteBy",
                description =
                        """
                                        A list of metadata fields to be used to search in previously added metadata. \
                                        Such retrieved metadata are then used to autofill fields that are null or \
                                        absent from the incoming payload as configured by the configuration property \
                                        registry.autocompletion-enabled-for. This is useful for EO that already \
                                        provided data to the registry to avoid continuing providing full metadata \
                                        payload in subsequent requests, if some properties are constant between \
                                        different products. As an example, assuming that it has been configured \
                                        registry.autocompletion-enabled-for=commodityCode,dataCarrierTypes,facilitiesId \
                                        providing the parameter as /registry?autocompleteBy=reoId will cause the \
                                        commodityCode, dataCarrierTypes and facilitiesId properties to be automatically \
                                        filled in the provided payload equal to the corresponding ones in the first \
                                        found metadata with the same reoId.
                                        """,
                in = ParameterIn.QUERY,
                required = false),
        @Parameter(
                name = "x-jws-signature",
                description =
                        """
                                        Detached JWS signature (RS256) over the raw request body in compact \
                                        serialisation with an empty payload segment: <header>..<signature>. \
                                        Required only when registry.jws.verification-enabled=true. \
                                        The signing key must be published at the JWKS URI carried in the JWT claim \
                                        whose name is configured by registry.jws.jwks-uri-claim-name (default: jwksUri).
                                        """,
                in = ParameterIn.HEADER,
                required = false)
    })
    @POST
    Uni<RestResponse<DPPMetadataEntry>> addDPPMetadata(
            @Context HttpHeaders headers, @RestQuery List<String> autocompleteBy, byte[] body);

    @Operation(
            summary = "Register DPP metadata",
            description =
                    """
                        Add or updates a DPP metadata entry to the registry (alternate path). \
                        Behaviour is identical to POST /metadata/v1. \
                        The payload is validated against the configured json schema before being persisted. \
                        The response always includes the added/updated metadata plus the registry id \
                        associated to them.
                        """)
    @Parameters({
        @Parameter(
                name = "autocompleteBy",
                description =
                        """
                                        A list of metadata fields to be used to search in previously added metadata. \
                                        Such retrieved metadata are then used to autofill fields that are null or \
                                        absent from the incoming payload as configured by the configuration property \
                                        registry.autocompletion-enabled-for. This is useful for EO that already \
                                        provided data to the registry to avoid continuing providing full metadata \
                                        payload in subsequent requests, if some properties are constant between \
                                        different products. As an example, assuming that it has been configured \
                                        registry.autocompletion-enabled-for=commodityCode,dataCarrierTypes,facilitiesId \
                                        providing the parameter as /registry?autocompleteBy=reoId will cause the \
                                        commodityCode, dataCarrierTypes and facilitiesId properties to be automatically \
                                        filled in the provided payload equal to the corresponding ones in the first \
                                        found metadata with the same reoId.
                                        """,
                in = ParameterIn.QUERY,
                required = false),
        @Parameter(
                name = "x-jws-signature",
                description =
                        """
                                        Detached JWS signature (RS256) over the raw request body in compact \
                                        serialisation with an empty payload segment: <header>..<signature>. \
                                        Required only when registry.jws.verification-enabled=true. \
                                        The signing key must be published at the JWKS URI carried in the JWT claim \
                                        whose name is configured by registry.jws.jwks-uri-claim-name (default: jwksUri).
                                        """,
                in = ParameterIn.HEADER,
                required = false)
    })
    @Path("/registerDPP")
    @POST
    Uni<RestResponse<DPPMetadataEntry>> registerDPP(
            @Context HttpHeaders headers, @RestQuery List<String> autocompleteBy, byte[] body);

    @Operation(
            summary = "Get Proof of Registration",
            description =
                    """
                        Returns a signed Proof of Registration JWT (RS256) for the registry entry \
                        identified by registryId. The JWT certifies that a specific DPP was registered \
                        in this registry at a given point in time. The payload includes: iss, \
                        registryId, registeredAt, commodityCode, reoId, reoName, dppHash, \
                        dppContentType. Verify the JWT signature using the public key published at \
                        GET /.well-known/jwks.json.
                        """)
    @Parameters({
        @Parameter(
                name = "registryId",
                description = "The unique registry identifier of the DPP entry.",
                in = ParameterIn.PATH,
                required = true),
        @Parameter(
                name = "reoId",
                description =
                        "Optional. Filters by Responsible Economic Operator ID to disambiguate"
                                + " entries that share the same registryId.",
                in = ParameterIn.QUERY,
                required = false)
    })
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        description =
                                "Compact JWT (application/jwt) signed with the registry's private"
                                        + " key."),
                @APIResponse(
                        responseCode = "404",
                        description = "No registry entry found for the given registryId.")
            })
    @Path("/{registryId}/proof")
    @GET
    @Produces("application/jwt")
    Uni<RestResponse<String>> getProofOrRegistration(
            @RestPath String registryId, @RestQuery Optional<String> reoId);
}
