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

import static it.extrared.registry.utils.CommonUtils.debug;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import it.extrared.registry.jsonschema.JsonSchemaService;
import it.extrared.registry.jsonschema.SchemaCache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
public class SchemaResourceImpl implements SchemaResource {
    @Inject JsonSchemaService service;
    @Inject SchemaCache schemaCache;

    private static final Logger LOGGER = Logger.getLogger(SchemaResourceImpl.class);

    @Override
    public Uni<RestResponse<Void>> addSchema(JsonNode node) {
        debug(
                LOGGER,
                () ->
                        "Controller method to add a new JSON schema invoked with body \n%s"
                                .formatted(node));
        return service.addSchema(node)
                .invoke(r -> schemaCache.invalidate())
                .map(n -> RestResponse.status(201));
    }

    @Override
    public Uni<RestResponse<JsonNode>> getCurrent() {
        debug(LOGGER, () -> "Controller method to get current JSON schema invoked");
        return service.getCurrentJsonSchema().map(RestResponse::ok);
    }

    @Override
    public Uni<RestResponse<Void>> removeCurrent() {
        debug(LOGGER, () -> "Controller method to remove current JSON schema invoked");
        Uni<RestResponse<Void>> res = service.removeLastSchema().map(v -> RestResponse.noContent());
        return res.invoke(r -> schemaCache.invalidate());
    }
}
