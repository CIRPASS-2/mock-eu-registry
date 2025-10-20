package it.extrared.registry.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;

/** Base interface for DPP metadata JSON schema repository. */
public interface JsonSchemaDBRepository {

    /**
     * Get the currently active DPP metadata JSON schema, i.e. the last one added.
     *
     * @return the DPP metadata JSON schema as a {@link io.smallrye.mutiny.Uni<JsonNode>}
     */
    Uni<JsonNode> getCurrentJsonSchema();

    /**
     * Add a new DPP metadata JSON schema to the repository.
     *
     * @param schema the JSON schema as a {@link com.fasterxml.jackson.databind.JsonNode}.
     * @return empty as a {@link io.smallrye.mutiny.Uni<Void>}
     */
    Uni<Void> addSchema(JsonNode schema);

    /**
     * Remove the last added JSON schema i.e. the currently active one.
     *
     * @return empty result as a {@link io.smallrye.mutiny.Uni<Void>}.
     */
    Uni<Void> removeLastSchema();
}
