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
package it.extrared.registry.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;

/**
 * Base interface for a JsonSchemaLoader, able to read a DPP metadata json schema document for a
 * given source.
 */
public interface JsonSchemaLoader {

    /**
     * Load the schema from the managed source and provides it as a JsonNode.
     *
     * @return the DPP metadata JSON schema as a {@link io.smallrye.mutiny.Uni<JsonNode>}.
     */
    Uni<JsonNode> loadSchema();

    /**
     * Return the priority of a schema loader over the others (the lower the number the greater the
     * priority).
     *
     * @return the priority of a schema loader as an {@link java.lang.Integer}.
     */
    Integer priority();

    /**
     * Set the next schema loader to be executed after this one in case this loader is not able to
     * retrieve the schema.
     *
     * @param nextLoader the next schema loader to be executed.
     */
    void setNext(JsonSchemaLoader nextLoader);
}
