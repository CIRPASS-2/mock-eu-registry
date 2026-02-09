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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.inject.spi.CDI;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.jboss.logging.Logger;

/** Some useful method to handle JSON data. */
public class JsonUtils {

    public static final String CONTEXT = "@context";
    public static final String GRAPH = "@graph";
    public static final String TYPE = "@type";
    public static final String VALUE = "@value";

    private static final Logger LOGGER = Logger.getLogger(JsonUtils.class);

    public static Map<String, Object> toMap(JsonNode node) {
        return objectMapper().convertValue(node, new TypeReference<Map<String, Object>>() {});
    }

    public static boolean nodeIsNotNull(JsonNode node) {
        return node != null && !node.isNull() && !node.isMissingNode();
    }

    public static JsonObject toVertxJson(JsonNode node) {
        return new JsonObject(toMap(node));
    }

    public static JsonNode fromVertxJson(JsonObject object) {
        return objectMapper().convertValue(object.getMap(), JsonNode.class);
    }

    public static ObjectMapper objectMapper() {
        return CDI.current().select(ObjectMapper.class).get();
    }

    public static JsonNode loadClasspathJsonTemplate(String fileName) throws IOException {
        try (InputStream is =
                JsonUtils.class.getResourceAsStream("/json-schema/%s".formatted(fileName))) {
            return objectMapper().readTree(is);
        }
    }
}
