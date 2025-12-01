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

import static it.extrared.registry.utils.CommonUtils.debug;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.exceptions.JsonSchemaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;
import org.jboss.logging.Logger;

/**
 * Implementation of a {@link JsonSchemaLoader} loading a json schema from a URI. The URI must be
 * provided using the configuration property {@link MetadataRegistryConfig#jsonSchemaLocation()}
 * i.e. registry.json-schema-location. The value must be either a file URI
 * (file:/path/to/the/schema.json), a relative file path or an HTTP url.
 */
@ApplicationScoped
public class URIJsonSchemaLoader extends AbstractJsonSchemaLoader {

    @Inject MetadataRegistryConfig config;
    @Inject ObjectMapper objectMapper;
    private static final Logger LOG = Logger.getLogger(JsonSchemaLoaderChain.class);

    @Override
    protected Uni<JsonNode> loadSchemaInternal() {
        Optional<String> location = config.jsonSchemaLocation();
        Uni<JsonNode> result;
        if (location.isEmpty()) {
            debug(
                    LOG,
                    () ->
                            "No configured location provided for a JSON schema. Ignoring %s"
                                    .formatted(getClass().getName()));
            result = Uni.createFrom().item(() -> null);
        } else {
            result =
                    Uni.createFrom()
                            .deferred(() -> Uni.createFrom().item(loadJsoNode(location.get())));
        }
        return result.runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private JsonNode loadJsoNode(String location) {
        URI uri = URI.create(location);

        if ("file".equalsIgnoreCase(uri.getScheme()) || uri.getScheme() == null) {
            debug(
                    LOG,
                    () ->
                            "Found configured location as a file URI %s. Trying to load the schema"
                                    .formatted(location));
            Path path = Paths.get(uri);
            return readStream(Unchecked.supplier(() -> Files.newInputStream(path)), location);

        } else {
            try {
                debug(
                        LOG,
                        () ->
                                "Found configured location %s. Trying to load the schema as a URL"
                                        .formatted(location));
                URL url = uri.toURL();
                return readStream(Unchecked.supplier(url::openStream), location);
            } catch (MalformedURLException e) {
                throw new JsonSchemaException("URL %s is malformed".formatted(location));
            }
        }
    }

    private JsonNode readStream(Supplier<InputStream> sup, String location) {
        try (InputStream is = sup.get()) {
            return objectMapper.readTree(is);
        } catch (IOException e) {
            throw new JsonSchemaException("Error while loading schema from %s".formatted(location));
        }
    }

    @Override
    public Integer priority() {
        return 999;
    }
}
