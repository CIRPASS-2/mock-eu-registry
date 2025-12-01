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

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import io.smallrye.mutiny.Uni;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.exceptions.JsonSchemaException;
import it.extrared.registry.utils.CommonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.jboss.logging.Logger;

/**
 * Chain of schema loader that executes the loaders by priority order untile a DPP metadata JSON
 * schema is retrieved.
 */
@ApplicationScoped
public class JsonSchemaLoaderChain {

    private JsonSchemaLoader head;

    private MetadataRegistryConfig config;

    private static final Function<JsonNode, JsonSchema> JN_TO_SCHEMA =
            jn -> JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(jn);

    private static final Logger LOG = Logger.getLogger(JsonSchemaLoaderChain.class);

    @Inject
    public JsonSchemaLoaderChain(
            Instance<JsonSchemaLoader> loaders, MetadataRegistryConfig config) {
        this.config = config;
        if (!loaders.isUnsatisfied()) {
            List<JsonSchemaLoader> list =
                    loaders.stream()
                            .sorted(Comparator.comparingInt(JsonSchemaLoader::priority))
                            .toList();
            this.head = buildChain(list);
        } else {
            throw new JsonSchemaException("No loader registered to retrieve a json schema");
        }
    }

    public JsonSchemaLoaderChain() {}

    /**
     * Load the DPP metadata schema executing loader by loader until one provides it.
     *
     * @return the {@link Schema} instance.
     */
    public Uni<Schema> loadSchema() {
        Uni<JsonSchema> uniSchema = head.loadSchema().map(JN_TO_SCHEMA);
        return uniSchema.map(
                s -> {
                    CommonUtils.debug(LOG, () -> "Valdating retrieved schema");
                    Schema schema = new Schema(s, config);
                    List<String> msgs = schema.validateSchemaCompliancy();
                    if (!msgs.isEmpty()) throw new JsonSchemaException(String.join(". ", msgs));
                    CommonUtils.debug(LOG, () -> "Schema is valid");
                    return schema;
                });
    }

    // builds the chain of schema loaders.
    private JsonSchemaLoader buildChain(List<JsonSchemaLoader> loaders) {
        Iterator<JsonSchemaLoader> it = loaders.iterator();
        if (!it.hasNext())
            throw new JsonSchemaException("No loader registered to retrieve a json schema");
        JsonSchemaLoader head = it.next();
        JsonSchemaLoader curr = head;
        while (it.hasNext()) {
            JsonSchemaLoader next = it.next();
            curr.setNext(next);
            curr = next;
        }
        return head;
    }
}
