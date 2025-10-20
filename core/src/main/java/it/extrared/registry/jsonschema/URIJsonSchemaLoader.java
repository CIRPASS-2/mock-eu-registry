package it.extrared.registry.jsonschema;

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

    @Override
    protected Uni<JsonNode> loadSchemaInternal() {
        Optional<String> location = config.jsonSchemaLocation();
        Uni<JsonNode> result;
        if (location.isEmpty()) {
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
            Path path = Paths.get(uri);
            return readStream(Unchecked.supplier(() -> Files.newInputStream(path)), location);

        } else {
            try {
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
        return 88;
    }
}
