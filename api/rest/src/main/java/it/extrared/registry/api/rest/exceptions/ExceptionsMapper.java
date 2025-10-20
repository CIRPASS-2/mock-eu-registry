package it.extrared.registry.api.rest.exceptions;

import it.extrared.registry.api.rest.RestUtils;
import it.extrared.registry.exceptions.JsonSchemaException;
import it.extrared.registry.exceptions.SchemaValidationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionsMapper {

    @ServerExceptionMapper
    public RestResponse<ErrorPayload> mapException(JsonSchemaException e) {
        return RestUtils.respWithBodyAndStatus(
                Response.Status.INTERNAL_SERVER_ERROR, new ErrorPayload(e.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorPayload> mapException(SchemaValidationException e) {
        return RestUtils.respWithBodyAndStatus(
                Response.Status.BAD_REQUEST,
                new ErrorPayload(
                        "The DPP metadata payload is not valid: %s".formatted(e.getMessage())));
    }
}
