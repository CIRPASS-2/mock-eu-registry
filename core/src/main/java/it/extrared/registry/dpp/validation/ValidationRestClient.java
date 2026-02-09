package it.extrared.registry.dpp.validation;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/validate/v1")
@RegisterRestClient(configKey = "dpp-validation")
public interface ValidationRestClient {

    @POST
    Uni<ValidationReport> validate(byte[] dpp, @HeaderParam("Content-Type") String contentType);
}
