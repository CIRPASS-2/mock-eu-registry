package it.extrared.registry.dpp.validation;

public class InvalidProperty {
    private String property;

    private String reason;

    public InvalidProperty(String property, String reason) {
        this.property = property;
        this.reason = reason;
    }

    public InvalidProperty() {}

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
