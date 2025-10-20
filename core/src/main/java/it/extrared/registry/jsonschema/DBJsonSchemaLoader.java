package it.extrared.registry.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Implementation of a {@link JsonSchemaLoader} loading the schema from a database using a {@link
 * JsonSchemaDBRepository}.
 */
@ApplicationScoped
public class DBJsonSchemaLoader extends AbstractJsonSchemaLoader {

    @Inject JsonSchemaDBRepository dbRepository;

    @Override
    protected Uni<JsonNode> loadSchemaInternal() {
        return dbRepository.getCurrentJsonSchema();
    }

    @Override
    public Integer priority() {
        return 11;
    }
}
