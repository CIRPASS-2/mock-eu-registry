package it.extrared.registry.api.rest.exceptions;

public class UnauthorizedJWS extends RuntimeException {

    public UnauthorizedJWS(String message) {
        super(message);
    }
}
