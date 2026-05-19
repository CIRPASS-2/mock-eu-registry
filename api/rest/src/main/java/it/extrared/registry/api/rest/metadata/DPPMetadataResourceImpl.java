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

import static it.extrared.registry.utils.CommonUtils.debug;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.api.rest.RestUtils;
import it.extrared.registry.api.rest.jws.JWSService;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.metadata.DPPMetadataService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
public class DPPMetadataResourceImpl implements DPPMetadataResource {

    @Inject DPPMetadataService service;
    @Inject JWSService jwsService;
    @Inject MetadataRegistryConfig config;

    @Inject ObjectMapper objectMapper;

    private static final Logger LOGGER = Logger.getLogger(DPPMetadataResourceImpl.class);

    @Override
    public Uni<RestResponse<DPPMetadataEntry>> addDPPMetadata(
            @Context HttpHeaders headers, @RestQuery List<String> autocompleteBy, byte[] body) {
        Uni<Void> jwsCheck = jwsService.verify(body, headers);
        return jwsCheck.flatMap(
                Unchecked.function(
                        v -> addDPPMetadataInternal(autocompleteBy, objectMapper.readTree(body))));
    }

    @Override
    public Uni<RestResponse<DPPMetadataEntry>> registerDPP(
            @Context HttpHeaders headers, @RestQuery List<String> autocompleteBy, byte[] body) {
        LOGGER.infof(
                "Resource method body length: %d, first byte: %d, last byte: %d",
                body.length, body[0], body[body.length - 1]);
        Uni<Void> jwsCheck = jwsService.verify(body, headers);
        return jwsCheck.flatMap(
                Unchecked.function(
                        v -> addDPPMetadataInternal(autocompleteBy, objectMapper.readTree(body))));
    }

    private Uni<RestResponse<DPPMetadataEntry>> addDPPMetadataInternal(
            List<String> autocompleteBy, JsonNode jsonNode) {
        debug(
                LOGGER,
                () ->
                        "Controller method to add new DPP metadata invoked with autocomplete by %s and body \n%s"
                                .formatted(autocompleteBy, jsonNode));
        return service.saveOrUpdate(jsonNode, autocompleteBy)
                .map(m -> RestUtils.respWithBodyAndStatus(Response.Status.CREATED, m));
    }
}
