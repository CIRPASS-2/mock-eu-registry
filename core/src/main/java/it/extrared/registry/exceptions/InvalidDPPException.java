package it.extrared.registry.exceptions;

import it.extrared.registry.dpp.validation.ValidationReport;

/**
 * Thrown when the DPP data retrieved from the live URL fails validation performed by the external
 * DPP validator service. Carries the full {@link ValidationReport} so callers can return it to the
 * client. Results in an HTTP {@code 400 Bad Request} response.
 */
public class InvalidDPPException extends RuntimeException {
    private ValidationReport validationReport;

    /**
     * Constructs a new {@code InvalidDPPException} wrapping the given validation report. The
     * exception message is taken from {@link ValidationReport#getMessage()}.
     *
     * @param validationReport the report produced by the DPP validator service.
     */
    public InvalidDPPException(ValidationReport validationReport) {
        super(validationReport.getMessage());
        this.validationReport = validationReport;
    }

    /**
     * Returns the validation report that caused this exception.
     *
     * @return the {@link ValidationReport} produced by the DPP validator.
     */
    public ValidationReport getValidationReport() {
        return validationReport;
    }

    /**
     * Sets the validation report associated with this exception.
     *
     * @param validationReport the new {@link ValidationReport}.
     */
    public void setValidationReport(ValidationReport validationReport) {
        this.validationReport = validationReport;
    }
}
