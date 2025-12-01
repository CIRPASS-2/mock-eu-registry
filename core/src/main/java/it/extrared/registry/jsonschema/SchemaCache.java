/*
 * Copyright 2024-2027 CIRPASS-2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.extrared.registry.jsonschema;

import static it.extrared.registry.utils.CommonUtils.debug;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;
import org.jboss.logging.Logger;

/** A cache for the schema currently in use. */
@ApplicationScoped
public class SchemaCache {

    private final AtomicReference<Uni<Schema>> cached = new AtomicReference<>();

    @Inject JsonSchemaLoaderChain loader;

    private static final Logger LOG = Logger.getLogger(SchemaCache.class);

    /**
     * @return the currently cache schema. If cache is empty the schema is first loaded and then
     *     cached.
     */
    public Uni<Schema> get() {
        Uni<Schema> schema = cached.get();
        if (schema == null) {
            debug(LOG, () -> "Caching JSON schema...");
            schema = loader.loadSchema().memoize().indefinitely();
            cached.compareAndSet(null, schema);
            debug(LOG, () -> "JSON schema cached...");
        }
        return schema;
    }

    /** Invalidates the cache. */
    public void invalidate() {
        debug(LOG, () -> "Invalidating JSON schema cache...");
        cached.set(null);
    }
}
