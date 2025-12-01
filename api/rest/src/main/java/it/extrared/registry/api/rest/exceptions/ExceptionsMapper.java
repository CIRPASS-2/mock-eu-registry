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
