package it.extrared.registry.exceptions;

/**
 * Thrown when a requested operation is not permitted according to the current registry
 * configuration or business rules (e.g., attempting to update a registry entry when the {@link
 * it.extrared.registry.metadata.update.UpdateType#NONE} strategy is active, or when a required JWS
 * header is missing). Results in an HTTP {@code 400 Bad Request} response.
 */
public class InvalidOperationException extends RuntimeException {

    /**
     * Constructs a new {@code InvalidOperationException} with the given detail message.
     *
     * @param message a human-readable description of the invalid operation.
     */
    public InvalidOperationException(String message) {
        super(message);
    }
}
