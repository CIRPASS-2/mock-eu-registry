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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.exceptions.JsonSchemaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import org.jboss.logging.Logger;

/**
 * Implementation of a {@link JsonSchemaLoader} loading the schema from the classpath (resources
 * folder).
 */
@ApplicationScoped
public class ClassPathJsonSchemaLoader extends AbstractJsonSchemaLoader {

    @Inject MetadataRegistryConfig config;
    @Inject ObjectMapper objectMapper;

    private static final Logger LOG = Logger.getLogger(ClassPathJsonSchemaLoader.class);

    @Override
    protected Uni<JsonNode> loadSchemaInternal() {
        Supplier<Uni<? extends JsonNode>> supplier =
                () -> Uni.createFrom().item(readFromClassPath());
        Uni<JsonNode> result = Uni.createFrom().deferred(supplier);
        return result.runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private JsonNode readFromClassPath() {
        String resourcePath = "/json-schema/%s".formatted(config.defaultTemplateName());
        debug(
                LOG,
                () ->
                        "Trying to load the default JSON schema from class path at %s"
                                .formatted(resourcePath));
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            return objectMapper.readTree(is);
        } catch (IOException e) {
            throw new JsonSchemaException(
                    "Error while reading the default json schema from class path.");
        }
    }

    @Override
    public Integer priority() {
        return Integer.MAX_VALUE;
    }
}
