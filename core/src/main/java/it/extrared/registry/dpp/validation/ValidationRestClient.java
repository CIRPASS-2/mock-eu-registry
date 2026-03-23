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
package it.extrared.registry.dpp.validation;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.smallrye.mutiny.Uni;
import it.extrared.registry.exceptions.ValidatorException;
import it.extrared.registry.security.AuthorizationHeaderForward;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** Rest client for the validation service. */
@Path("/validate/v1")
@RegisterClientHeaders(AuthorizationHeaderForward.class)
@RegisterRestClient(configKey = "dpp-validation")
public interface ValidationRestClient {

    /**
     * Issue an http request to the validation service to validate a dpp.
     *
     * @param dpp the dpp byte[]
     * @param contentType the content type.
     * @return a validation report as a {@link ValidationReport}
     */
    @POST
    Uni<ValidationReport> validate(byte[] dpp, @HeaderParam("Content-Type") String contentType);

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        return switch (response.getStatus()) {
            case 500 -> {
                String message = "Validator replied with error.\n";
                if (response.hasEntity()) {
                    message = message.concat(response.readEntity(String.class));
                }
                yield new ValidatorException(message);
            }
            case 401 -> new ValidatorException("Unauthorized access to validation service.");
            case 403 -> new ValidatorException("Forbidden access to validation service.");
            case 404 ->
                    new NotFoundException(
                            "No matching validation resource found. The registry entry cannot be validated.");
            case 400 -> new ValidatorException("Bad request from registry to validation service.");
            default ->
                    new ValidatorException(
                            "Something wrong happened while invoking the validation service.");
        };
    }
}
