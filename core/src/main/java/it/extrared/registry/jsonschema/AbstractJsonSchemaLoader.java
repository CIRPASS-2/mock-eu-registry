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
package it.extrared.registry.jsonschema;

import static it.extrared.registry.utils.CommonUtils.debug;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import java.util.function.Function;
import org.jboss.logging.Logger;

/**
 * Abstract implementation of a {@link it.extrared.registry.jsonschema.JsonSchemaLoader} providing
 * reusable logic between different schema loaders.
 */
public abstract class AbstractJsonSchemaLoader implements JsonSchemaLoader {

    protected JsonSchemaLoader next;

    private static final Logger LOG = Logger.getLogger(AbstractJsonSchemaLoader.class);

    @Override
    public void setNext(JsonSchemaLoader nextLoader) {
        this.next = nextLoader;
    }

    @Override
    public Uni<JsonNode> loadSchema() {
        Uni<JsonNode> schema = loadSchemaInternal();
        Function<JsonNode, Uni<? extends JsonNode>> doNextWhenNull =
                jn -> {
                    if (jn == null) {
                        debug(
                                LOG,
                                () ->
                                        "No JSON schema found with loader %s, trying next loader..."
                                                .formatted(getClass().getName()));
                        return next.loadSchema();
                    } else {
                        debug(
                                LOG,
                                () ->
                                        "JSON schema found with loader %s"
                                                .formatted(getClass().getName()));
                        return Uni.createFrom().item(() -> jn);
                    }
                };
        return schema.flatMap(doNextWhenNull);
    }

    /**
     * Subclasses must implement this method to perform their custom logic that actually loads the
     * json schema.
     *
     * @return a json schema as a {@link io.smallrye.mutiny.Uni<JsonNode>}
     */
    protected abstract Uni<JsonNode> loadSchemaInternal();
}
