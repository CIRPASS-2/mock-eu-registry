package it.extrared.registry.dpp.validation;

import java.util.List;

/** Represent the validation report of the validator service. */
public class ValidationReport {

    private boolean valid;

    private String message;

    private String validatedWith;

    private String validationType;

    private List<InvalidProperty> invalidProperties;

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public List<InvalidProperty> getInvalidProperties() {
        return invalidProperties;
    }

    public String getValidatedWith() {
        return validatedWith;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setValidatedWith(String validatedWith) {
        this.validatedWith = validatedWith;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public void setInvalidProperties(List<InvalidProperty> invalidProperties) {
        this.invalidProperties = invalidProperties;
    }
}
