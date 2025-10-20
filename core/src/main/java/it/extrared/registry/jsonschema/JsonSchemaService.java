package it.extrared.registry.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JsonSchemaService {

    @Inject JsonSchemaDBRepository repository;

    public Uni<JsonNode> getCurrentJsonSchema() {
        return repository.getCurrentJsonSchema();
    }

    public Uni<Void> addSchema(JsonNode node) {
        return repository.addSchema(node);
    }

    public Uni<Void> removeLastSchema() {
        return repository.removeLastSchema();
    }
}
