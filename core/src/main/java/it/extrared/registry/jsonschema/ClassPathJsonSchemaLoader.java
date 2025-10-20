package it.extrared.registry.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import it.extrared.registry.exceptions.JsonSchemaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Implementation of a {@link JsonSchemaLoader} loading the schema from the classpath (resources
 * folder).
 */
@ApplicationScoped
public class ClassPathJsonSchemaLoader extends AbstractJsonSchemaLoader {

    @Inject ObjectMapper objectMapper;

    @Override
    protected Uni<JsonNode> loadSchemaInternal() {
        Supplier<Uni<? extends JsonNode>> supplier =
                () -> Uni.createFrom().item(readFromClassPath());
        Uni<JsonNode> result = Uni.createFrom().deferred(supplier);
        return result.runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private JsonNode readFromClassPath() {
        try (InputStream is = getClass().getResourceAsStream("/json-schema/default-schema.json")) {
            return objectMapper.readTree(is);
        } catch (IOException e) {
            throw new JsonSchemaException(
                    "Error while reading the default json schema from class path.");
        }
    }

    @Override
    public Integer priority() {
        return 99;
    }
}
