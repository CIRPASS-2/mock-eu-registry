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

import com.fasterxml.jackson.databind.JsonNode;
import it.extrared.registry.metadata.DPPMetadataEntry;

/** A metadata entry decorated with a validation report. */
public class ValidatedMetadataEntry extends DPPMetadataEntry {

    private ValidationReport validation;

    public ValidatedMetadataEntry(JsonNode metadata, ValidationReport validation) {
        super(metadata);
        this.validation = validation;
    }

    public ValidatedMetadataEntry(DPPMetadataEntry baseEntry, ValidationReport report) {
        super(baseEntry.getMetadata());
        setRegistryId(baseEntry.getRegistryId());
        setCreatedAt(baseEntry.getCreatedAt());
        setModifiedAt(baseEntry.getModifiedAt());
        setValidation(report);
    }

    public ValidatedMetadataEntry() {}

    public ValidationReport getValidation() {
        return validation;
    }

    public void setValidation(ValidationReport validation) {
        this.validation = validation;
    }
}
