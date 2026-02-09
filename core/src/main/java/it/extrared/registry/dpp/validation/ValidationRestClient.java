package it.extrared.registry.dpp.validation;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** Rest client for the validation service. */
@Path("/validate/v1")
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
}
