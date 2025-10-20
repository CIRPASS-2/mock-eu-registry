package it.extrared.registry.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.inject.spi.CDI;
import java.util.Map;

/** Some useful method to handle JSON data. */
public class JsonUtils {

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
}
