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
