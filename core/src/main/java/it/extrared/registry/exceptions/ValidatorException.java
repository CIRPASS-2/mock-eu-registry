package it.extrared.registry.exceptions;

/**
 * Thrown when the external DPP validator service returns an unexpected error or is unreachable.
 * Results in an HTTP {@code 500 Internal Server Error} response.
 */
public class ValidatorException extends RuntimeException {

    /**
     * Constructs a new {@code ValidatorException} with the given detail message.
     *
     * @param message a human-readable description of the validation service error.
     */
    public ValidatorException(String message) {
        super(message);
    }
}
