package it.extrared.registry.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import io.smallrye.mutiny.Uni;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.exceptions.JsonSchemaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Chain of schema loader that executes the loaders by priority order untile a DPP metadata JSON
 * schema is retrieved.
 */
@ApplicationScoped
public class JsonSchemaLoaderChain {

    private JsonSchemaLoader head;

    private MetadataRegistryConfig config;

    private static final Function<JsonNode, JsonSchema> JN_TO_SCHEMA =
            jn -> JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(jn);

    @Inject
    public JsonSchemaLoaderChain(
            Instance<JsonSchemaLoader> loaders, MetadataRegistryConfig config) {
        this.config = config;
        if (!loaders.isUnsatisfied()) {
            List<JsonSchemaLoader> list =
                    loaders.stream()
                            .sorted(Comparator.comparingInt(JsonSchemaLoader::priority))
                            .toList();
            this.head = buildChain(list);
        } else {
            throw new JsonSchemaException("No loader registered to retrieve a json schema");
        }
    }

    public JsonSchemaLoaderChain() {}

    /**
     * Load the DPP metadata schema executing loader by loader until one provides it.
     *
     * @return the {@link Schema} instance.
     */
    public Uni<Schema> loadSchema() {
        Uni<JsonSchema> uniSchema = head.loadSchema().map(JN_TO_SCHEMA);
        return uniSchema.map(
                s -> {
                    Schema schema = new Schema(s, config);
                    List<String> msgs = schema.validateSchemaCompliancy();
                    if (!msgs.isEmpty()) throw new JsonSchemaException(String.join(". ", msgs));
                    return schema;
                });
    }

    // builds the chain of schema loaders.
    private JsonSchemaLoader buildChain(List<JsonSchemaLoader> loaders) {
        Iterator<JsonSchemaLoader> it = loaders.iterator();
        if (!it.hasNext())
            throw new JsonSchemaException("No loader registered to retrieve a json schema");
        JsonSchemaLoader head = it.next();
        JsonSchemaLoader curr = head;
        while (it.hasNext()) {
            JsonSchemaLoader next = it.next();
            curr.setNext(next);
            curr = next;
        }
        return head;
    }
}
