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
package it.extrared.registry.mocks;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import it.extrared.registry.jsonschema.JsonSchemaDBRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Unremovable
public class EmptyJsonSchemaDBRepository implements JsonSchemaDBRepository {
    @Override
    public Uni<JsonNode> getCurrentJsonSchema() {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<Void> addSchema(JsonNode schema) {
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> removeLastSchema() {
        return Uni.createFrom().voidItem();
    }
}
