package it.extrared.registry.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import java.util.function.Function;

/**
 * Abstract implementation of a {@link it.extrared.registry.jsonschema.JsonSchemaLoader} providing
 * reusable logic between different schema loaders.
 */
public abstract class AbstractJsonSchemaLoader implements JsonSchemaLoader {

    protected JsonSchemaLoader next;

    @Override
    public void setNext(JsonSchemaLoader nextLoader) {
        this.next = nextLoader;
    }

    @Override
    public Uni<JsonNode> loadSchema() {
        Uni<JsonNode> schema = loadSchemaInternal();
        Function<JsonNode, Uni<? extends JsonNode>> doNextWhenNull =
                jn -> {
                    if (jn == null) return next.loadSchema();
                    else return Uni.createFrom().item(() -> jn);
                };
        return schema.flatMap(doNextWhenNull);
    }

    /**
     * Subclasses must implement this method to perform their custom logic that actually loads the
     * json schema.
     *
     * @return a json schema as a {@link io.smallrye.mutiny.Uni<JsonNode>}
     */
    protected abstract Uni<JsonNode> loadSchemaInternal();
}
