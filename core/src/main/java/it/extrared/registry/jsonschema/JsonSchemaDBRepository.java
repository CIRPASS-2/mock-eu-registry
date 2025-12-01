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

/** Base interface for DPP metadata JSON schema repository. */
public interface JsonSchemaDBRepository {

    /**
     * Get the currently active DPP metadata JSON schema, i.e. the last one added.
     *
     * @return the DPP metadata JSON schema as a {@link io.smallrye.mutiny.Uni<JsonNode>}
     */
    Uni<JsonNode> getCurrentJsonSchema();

    /**
     * Add a new DPP metadata JSON schema to the repository.
     *
     * @param schema the JSON schema as a {@link com.fasterxml.jackson.databind.JsonNode}.
     * @return empty as a {@link io.smallrye.mutiny.Uni<Void>}
     */
    Uni<Void> addSchema(JsonNode schema);

    /**
     * Remove the last added JSON schema i.e. the currently active one.
     *
     * @return empty result as a {@link io.smallrye.mutiny.Uni<Void>}.
     */
    Uni<Void> removeLastSchema();
}
