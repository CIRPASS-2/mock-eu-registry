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

import static it.extrared.registry.utils.CommonUtils.debug;
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
import org.jboss.logging.Logger;

/**
 * Class providing funcionality to validate a DPP associated to a DPP metadata entry (via live url).
 */
@ApplicationScoped
public class DPPValidator {

    private static final Logger LOGGER = Logger.getLogger(DPPValidator.class);

    @Inject DPPFetcher dppFetcher;

    @Inject @RestClient ValidationRestClient validationRestClient;

    @Inject MetadataRegistryConfig config;

    /**
     * Given a {@link DPPMetadataEntry} retrieved the associated DPP data through its live URL.
     *
     * @param entry the registry entry.
     * @return the entry possbily decorated with the validation report.
     */
    public Uni<DPPMetadataEntry> validate(DPPMetadataEntry entry) {
        String url = getUrl(entry);
        debug(LOGGER, () -> "retrieved URL for decentralized repository %s".formatted(url));
        if (url == null)
            throw new SchemaValidationException(
                    "Expected to find a live url in metadata under field name %s but did not find any."
                            .formatted(config.liveUrlFieldName()));
        return dppFetcher
                .fetchDPP(url)
                .flatMap(this::validate)
                .map(r -> new ValidatedMetadataEntry(entry, r));
    }

    private Uni<ValidationReport> validate(HttpResponse<Buffer> dppResp) {
        if (is2xx(dppResp.statusCode())) {
            String cType = dppResp.getHeader("Content-Type");
            byte[] body = dppResp.bodyAsBuffer().getBytes();
            debug(
                    LOGGER,
                    () ->
                            "received DPP with content type %s and payload %s"
                                    .formatted(cType, new String(body)));
            Uni<ValidationReport> validationReportUni = validationRestClient.validate(body, cType);
            return validationReportUni.invoke(
                    r -> {
                        debug(LOGGER, () -> "obtained validation response...");
                        if (!r.isValid()) throw new InvalidDPPException(r);
                    });
        } else {
            throw new RuntimeException(
                    "Error while retrieving DPP from live URL for validation. Server replied with status %s and message %s"
                            .formatted(dppResp.statusCode(), dppResp.bodyAsString()));
        }
    }

    private String getUrl(DPPMetadataEntry entry) {
        debug(
                LOGGER,
                () ->
                        "Trying retrieving the live URL using field name %s"
                                .formatted(config.liveUrlFieldName()));
        if (entry.getMetadata() != null && entry.getMetadata().has(config.liveUrlFieldName()))
            return entry.getMetadata().get(config.liveUrlFieldName()).asText();
        return null;
    }
}
