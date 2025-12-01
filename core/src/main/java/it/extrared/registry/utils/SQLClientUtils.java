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
package it.extrared.registry.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import it.extrared.registry.metadata.DPPMetadataEntry;
import java.io.IOException;
import java.util.Iterator;

/** Some useful methods for code using Reactive SQL client. */
public class SQLClientUtils {

    /**
     * @param rs the result set of a query.
     * @return the first result in the set if any or null.
     */
    public static DPPMetadataEntry firstOrNull(RowSet<DPPMetadataEntry> rs) {
        RowIterator<DPPMetadataEntry> it = rs.iterator();
        if (it.hasNext()) return it.next();
        else return null;
    }

    /**
     * Gets a JSON value from the iterator argument if any or returns null.
     *
     * @param objectMapper the jackson object mapper
     * @param rs the result set.
     * @return a {@link JsonNode} representing the JSON value.
     */
    public static JsonNode getJsonNode(ObjectMapper objectMapper, Iterator<byte[]> rs)
            throws IOException {
        if (rs.hasNext()) {
            byte[] next = rs.next();
            if (next != null) return objectMapper.readTree(next);
        }
        return null;
    }

    /**
     * Gets a JSON value from the iterator argument if any or returns null.
     *
     * @param rs the result set.
     * @return a {@link JsonNode} representing the JSON value.
     */
    public static JsonNode getJsonNode(Iterator<JsonObject> rs) throws IOException {
        if (rs.hasNext()) {
            JsonObject jo = rs.next();
            if (jo != null) return JsonUtils.fromVertxJson(jo);
        }
        return null;
    }
}
