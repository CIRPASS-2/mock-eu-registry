package it.extrared.registry.exceptions;

/** Exception used to signal error in retrieving and managing a JsonSchema. */
public class JsonSchemaException extends RuntimeException {

    public JsonSchemaException(String message) {
        super("Unabled to load json schema: %s".formatted(message));
    }
}
