package it.extrared.registry.api.rest.schema;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import it.extrared.registry.jsonschema.JsonSchemaService;
import it.extrared.registry.jsonschema.SchemaCache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
public class SchemaResourceImpl implements SchemaResource {
    @Inject JsonSchemaService service;
    @Inject SchemaCache schemaCache;

    @Override
    public Uni<RestResponse<Void>> addSchema(JsonNode node) {
        return service.addSchema(node)
                .invoke(r -> schemaCache.invalidate())
                .map(n -> RestResponse.status(201));
    }

    @Override
    public Uni<RestResponse<JsonNode>> getCurrent() {
        return service.getCurrentJsonSchema().map(RestResponse::ok);
    }

    @Override
    public Uni<RestResponse<Void>> removeCurrent() {
        return service.removeLastSchema()
                .map(RestResponse::accepted)
                .invoke(r -> schemaCache.invalidate());
    }
}
