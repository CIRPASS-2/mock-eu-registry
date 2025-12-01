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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import it.extrared.registry.TestSupport;
import it.extrared.registry.utils.JsonUtils;
import jakarta.inject.Inject;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class JsonSchemaLoaderTest extends TestSupport {

    @Inject SchemaCache schemaCache;
    @Inject ObjectMapper objectMapper;

    @InjectMock JsonSchemaDBRepository repository;

    @BeforeEach
    public void before() {
        schemaCache.invalidate();
    }

    @Test
    @RunOnVertxContext
    public void testDefaultLoading(UniAsserter asserter) throws IOException {
        Mockito.when(repository.getCurrentJsonSchema()).thenReturn(Uni.createFrom().nullItem());
        JsonNode test =
                objectMapper.readTree(
                        getClass().getResourceAsStream("/json-schema/core-default-schema.json"));
        asserter.assertEquals(() -> schemaCache.get().map(Schema::getSchema), test);
    }

    @Test
    @RunOnVertxContext
    public void testDbLoading(UniAsserter asserter) throws IOException {
        Mockito.when(repository.getCurrentJsonSchema())
                .thenReturn(
                        Uni.createFrom()
                                .item(JsonUtils.loadClasspathJsonTemplate("db-schema.json")));
        JsonNode test =
                objectMapper.readTree(
                        getClass().getResourceAsStream("/json-schema/db-schema.json"));
        asserter.assertEquals(() -> schemaCache.get().map(Schema::getSchema), test);
    }
}
