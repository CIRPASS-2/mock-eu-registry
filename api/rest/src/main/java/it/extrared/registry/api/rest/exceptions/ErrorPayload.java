package it.extrared.registry.api.rest.exceptions;

public class ErrorPayload {

    private String message;

    public ErrorPayload() {}

    public ErrorPayload(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
