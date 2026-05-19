package it.extrared.registry.api.rest.exceptions;

public class JWSVerificationException extends RuntimeException {

    public JWSVerificationException(String message) {
        super(message);
    }
}
