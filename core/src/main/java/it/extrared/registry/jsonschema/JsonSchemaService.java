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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Service layer for JSON Schema management operations. Delegates persistence to {@link
 * JsonSchemaDBRepository}.
 */
@ApplicationScoped
public class JsonSchemaService {

    @Inject JsonSchemaDBRepository repository;

    /**
     * Returns the most recently submitted JSON Schema stored in the database.
     *
     * @return a {@link Uni} emitting the current schema as a {@link JsonNode}, or {@code null} if
     *     none has been persisted yet.
     */
    public Uni<JsonNode> getCurrentJsonSchema() {
        return repository.getCurrentJsonSchema();
    }

    /**
     * Persists a new JSON Schema, making it the active schema for subsequent validations.
     *
     * @param node the JSON Schema document to store.
     * @return a {@link Uni} that completes when the schema has been persisted.
     */
    public Uni<Void> addSchema(JsonNode node) {
        return repository.addSchema(node);
    }

    /**
     * Removes the most recently submitted JSON Schema from the database. After removal, the
     * previous schema in the resolution chain becomes active.
     *
     * @return a {@link Uni} that completes when the schema has been removed.
     */
    public Uni<Void> removeLastSchema() {
        return repository.removeLastSchema();
    }
}
