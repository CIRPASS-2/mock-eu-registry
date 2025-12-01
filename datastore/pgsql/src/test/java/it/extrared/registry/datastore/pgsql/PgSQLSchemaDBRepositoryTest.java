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
package it.extrared.registry.datastore.pgsql;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import it.extrared.registry.jsonschema.JsonSchemaDBRepository;
import it.extrared.registry.utils.JsonUtils;
import jakarta.inject.Inject;
import java.io.IOException;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PgSQLSchemaDBRepositoryTest {

    @Inject JsonSchemaDBRepository schemaDBRepository;

    @Test
    @RunOnVertxContext
    public void testAddNewGetRemoveGet(UniAsserter asserter) throws IOException {
        JsonNode schema = JsonUtils.loadClasspathJsonTemplate("db-schema.json");
        JsonNode current = JsonUtils.loadClasspathJsonTemplate("pgsql-default-schema.json");
        asserter.assertNull(() -> schemaDBRepository.addSchema(schema));
        asserter.assertEquals(() -> schemaDBRepository.getCurrentJsonSchema(), schema);
        asserter.assertNull(() -> schemaDBRepository.removeLastSchema());
        asserter.assertEquals(() -> schemaDBRepository.getCurrentJsonSchema(), current);
    }
}
