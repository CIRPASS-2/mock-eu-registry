package it.extrared.registry.dpp.validation;

import static it.extrared.registry.utils.CommonUtils.is2xx;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.dpp.DPPFetcher;
import it.extrared.registry.exceptions.InvalidDPPException;
import it.extrared.registry.exceptions.SchemaValidationException;
import it.extrared.registry.metadata.DPPMetadataEntry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class DPPValidator {

    @Inject DPPFetcher dppFetcher;

    @Inject @RestClient ValidationRestClient validationRestClient;

    @Inject MetadataRegistryConfig config;

    public Uni<DPPMetadataEntry> validate(DPPMetadataEntry entry) {
        String url = getUrl(entry);
        if (url == null)
            throw new SchemaValidationException(
                    "Expected to find a live url in metadata under field name %s but did not find any."
                            .formatted(config.liveUrlFieldName()));
        return dppFetcher
                .fetchDPP(url)
                .flatMap(this::validate)
                .map(r -> new ValidatedMetadataEntry(entry, r));
    }

    public Uni<ValidationReport> validate(HttpResponse<Buffer> dppResp) {
        if (is2xx(dppResp.statusCode())) {
            String cType = dppResp.getHeader("Content-Type");
            byte[] body = dppResp.bodyAsBuffer().getBytes();
            Uni<ValidationReport> validationReportUni = validationRestClient.validate(body, cType);
            return validationReportUni.invoke(
                    r -> {
                        if (!r.isValid()) throw new InvalidDPPException(r);
                    });
        } else {
            throw new RuntimeException(
                    "Error while retrieving DPP from live URL for validation. Server replied with status %s and message %s"
                            .formatted(dppResp.statusCode(), dppResp.bodyAsString()));
        }
    }

    private String getUrl(DPPMetadataEntry entry) {
        if (entry.getMetadata() != null && entry.getMetadata().has(config.liveUrlFieldName()))
            return entry.getMetadata().get(config.liveUrlFieldName()).asText();
        return null;
    }
}
