package it.extrared.registry.exceptions;

/**
 * Thrown when a requested resource (e.g., a metadata entry identified by its registry ID) cannot be
 * found in the registry. Results in an HTTP {@code 404 Not Found} response.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code NotFoundException} with the given detail message.
     *
     * @param message a human-readable description of the missing resource.
     */
    public NotFoundException(String message) {
        super(message);
    }
}
