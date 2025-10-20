package it.extrared.registry.jsonschema;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

/** A cache for the schema currently in use. */
@ApplicationScoped
public class SchemaCache {

    private final AtomicReference<Uni<Schema>> cached = new AtomicReference<>();

    @Inject JsonSchemaLoaderChain loader;

    /**
     * @return the currently cache schema. If cache is empty the schema is first loaded and then
     *     cached.
     */
    public Uni<Schema> get() {
        Uni<Schema> schema = cached.get();
        if (schema == null) {
            schema = loader.loadSchema().memoize().indefinitely();
            cached.compareAndSet(null, schema);
        }
        return schema;
    }

    /** Invalidates the cache. */
    public void invalidate() {
        cached.set(null);
    }
}
