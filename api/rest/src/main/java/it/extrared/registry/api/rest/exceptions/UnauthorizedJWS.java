package it.extrared.registry.api.rest.exceptions;

/**
 * Thrown when the caller is not authorized to perform an operation that requires a valid detached
 * JWS, for example when the JWS header is present but the signing key is not trusted. Results in an
 * HTTP {@code 401 Unauthorized} response.
 */
public class UnauthorizedJWS extends RuntimeException {

    /**
     * Constructs a new {@code UnauthorizedJWS} with the given detail message.
     *
     * @param message a human-readable description of the authorization failure.
     */
    public UnauthorizedJWS(String message) {
        super(message);
    }
}
