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

import io.smallrye.mutiny.Uni;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.exceptions.InvalidDPPException;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.metadata.DppWithCType;
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

    @Inject @RestClient ValidationRestClient validationRestClient;

    @Inject MetadataRegistryConfig config;

    /**
     * Given a {@link DPPMetadataEntry} retrieved the associated DPP data through its live URL.
     *
     * @param entry the registry entry.
     * @return the entry possbily decorated with the validation report.
     */
    public Uni<DPPMetadataEntry> validate(DPPMetadataEntry entry, DppWithCType dppWithCType) {
        return validate(dppWithCType).map(r -> new ValidatedMetadataEntry(entry, r));
    }

    private Uni<ValidationReport> validate(DppWithCType dppWithCType) {
        Uni<ValidationReport> validationReportUni =
                validationRestClient.validate(dppWithCType.body(), dppWithCType.contentType());
        return validationReportUni.invoke(
                r -> {
                    debug(LOGGER, () -> "obtained validation response...");
                    if (!r.isValid()) throw new InvalidDPPException(r);
                });
    }
}
