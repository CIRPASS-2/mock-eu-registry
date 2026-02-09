package it.extrared.registry.exceptions;

import it.extrared.registry.dpp.validation.ValidationReport;

public class InvalidDPPException extends RuntimeException {
    private ValidationReport validationReport;

    public InvalidDPPException(ValidationReport validationReport) {
        super(validationReport.getMessage());
        this.validationReport = validationReport;
    }

    public ValidationReport getValidationReport() {
        return validationReport;
    }

    public void setValidationReport(ValidationReport validationReport) {
        this.validationReport = validationReport;
    }
}
