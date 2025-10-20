package it.extrared.registry.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * Merge two JSON object overriding properties in a JSON with the values of corresponding properties
 * of a second JSON if those properties are non-null.
 */
public class JsonMerger {

    /**
     * Merge two JSON following the below logic: - if overlay JSON has a non-null non-empty
     * primitive value property that value is set to the corresponding property in the base JSON. -
     * if the overlay JSON has a non-null non-empty array of primitive property the values in the
     * array are added to the corresponding array of properties in the base node.
     *
     * @param base base JSON to be overrided.
     * @param overlay overlay JSON providing overriding values.
     * @return the merged result i.e. the base JSON with updated values.
     */
    public JsonNode merge(ObjectNode base, ObjectNode overlay) {
        Iterator<String> fields = overlay.fieldNames();
        while (fields.hasNext()) {
            String field = fields.next();
            JsonNode overlayVal = overlay.get(field);
            JsonNode baseVal = base.get(field);
            JsonNode newVal = newValue(baseVal, overlayVal);
            base.set(field, newVal);
        }
        return base;
    }

    private JsonNode newValue(JsonNode baseVal, JsonNode overlayVal) {
        JsonNode ret;
        if (baseVal.isNull() && overlayVal.isNull()) {
            ret = baseVal;
        } else if (baseVal.isArray() && overlayVal.isArray()) {
            ArrayNode baseArr = (ArrayNode) baseVal;
            ArrayNode overArr = (ArrayNode) overlayVal;
            Set<JsonNode> existing = new HashSet<>();
            baseArr.forEach(existing::add);

            overArr.forEach(
                    node -> {
                        if (!existing.contains(node)) {
                            baseArr.add(node);
                        }
                    });
            ret = baseArr;
        } else if (baseVal.isValueNode() && overlayVal.isValueNode()) {
            ret = !Objects.equals(baseVal, overlayVal) ? overlayVal : baseVal;
        } else {
            throw new UnsupportedOperationException(
                    "The API supports JSON data only if properties have raw types or array of raw types");
        }

        return ret;
    }
}
