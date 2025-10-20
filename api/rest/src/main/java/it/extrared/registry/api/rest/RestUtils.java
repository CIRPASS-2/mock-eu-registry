package it.extrared.registry.api.rest;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;

public class RestUtils {

    public static <T> RestResponse<T> respWithBodyAndStatus(Response.Status status, T body) {
        RestResponse.ResponseBuilder<T> builder = RestResponse.ResponseBuilder.create(status);
        return builder.entity(body).build();
    }
}
