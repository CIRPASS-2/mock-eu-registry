package it.extrared.registry.api.rest.exceptions;

/**
 * Thrown when the detached JWS signature verification fails. This can occur if the signature is
 * invalid, the JWS format is malformed, or an error occurs while retrieving the JWKS. Results in an
 * HTTP {@code 401 Unauthorized} response.
 */
public class JWSVerificationException extends RuntimeException {

    /**
     * Constructs a new {@code JWSVerificationException} with the given detail message.
     *
     * @param message a human-readable description of the verification failure.
     */
    public JWSVerificationException(String message) {
        super(message);
    }
}
