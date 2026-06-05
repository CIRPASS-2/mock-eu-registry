/*
 * Copyright 2024-2027 CIRPASS-2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.extrared.registry.api.rest;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;

/** Utility class providing helper methods for building JAX-RS REST responses. */
public class RestUtils {

    /**
     * Builds a {@link RestResponse} with the given HTTP status code and response body.
     *
     * @param <T> the type of the response entity.
     * @param status the HTTP status to set on the response.
     * @param body the entity to include in the response body.
     * @return a {@link RestResponse} carrying {@code body} with {@code status}.
     */
    public static <T> RestResponse<T> respWithBodyAndStatus(Response.Status status, T body) {
        RestResponse.ResponseBuilder<T> builder = RestResponse.ResponseBuilder.create(status);
        return builder.entity(body).build();
    }
}
