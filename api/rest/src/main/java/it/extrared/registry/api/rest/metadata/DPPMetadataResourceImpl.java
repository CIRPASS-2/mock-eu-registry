/*
 * Copyright 2025-2026 ExtraRed
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
import io.smallrye.mutiny.Uni;
import it.extrared.registry.api.rest.RestUtils;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.metadata.DPPMetadataService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
public class DPPMetadataResourceImpl implements DPPMetadataResource {
    @Inject DPPMetadataService service;

    private static final Logger LOGGER = Logger.getLogger(DPPMetadataResourceImpl.class);

    @Override
    public Uni<RestResponse<DPPMetadataEntry>> addDPPMetadata(
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
