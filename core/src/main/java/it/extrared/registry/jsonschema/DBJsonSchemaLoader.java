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
 * Implementation of a {@link JsonSchemaLoader} loading the schema from a database using a {@link
 * JsonSchemaDBRepository}.
 */
@ApplicationScoped
public class DBJsonSchemaLoader extends AbstractJsonSchemaLoader {

    @Inject JsonSchemaDBRepository dbRepository;

    @Override
    protected Uni<JsonNode> loadSchemaInternal() {
        return dbRepository.getCurrentJsonSchema();
    }

    @Override
    public Integer priority() {
        return 99;
    }
}
