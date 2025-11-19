/*
 * Copyright 2025-2026 ExtraRed
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
import static it.extrared.registry.utils.JsonUtils.nodeIsNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import it.extrared.registry.MetadataRegistryConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jboss.logging.Logger;

/**
 * A wrapper of a {@link com.networknt.schema.JsonSchema} that adds some method for validation and
 * for schema fields retrieval.
 */
public class Schema {

    private final JsonSchema schema;

    public static final String TYPE_KEY = "type";

    public static final String ITEMS_KEY = "items";

    public static final String PROPERTIES_KEY = "properties";

    public static final Set<String> PRIMITIVE_TYPES =
            Set.of("string", "number", "integer", "boolean", "null");

    private final MetadataRegistryConfig config;

    private static final Logger LOG = Logger.getLogger(Schema.class);

    public Schema(JsonSchema schema, MetadataRegistryConfig config) {
        this.schema = schema;
        this.config = config;
    }

    /**
     * Validate a JSON against the underlying JSON schema.
     *
     * @param data the data to validate.
     * @return the set of {@link ValidationMessage}, empty if the validation was successful.
     */
    public Set<ValidationMessage> validateJson(JsonNode data) {
        return schema.validate(data);
    }

    public JsonNode getSchema() {
        return schema.getSchemaNode();
    }

    public String getPropertyType(String propertyName) {
        JsonNode node = schema.getSchemaNode().get(PROPERTIES_KEY).get(propertyName);
        if (nodeIsNotNull(node)) {
            JsonNode type = node.get(TYPE_KEY);
            if (nodeIsNotNull(type) && type.isTextual()) {
                return type.asText();
            } else if (type.isArray()) {
                ArrayNode arrayNode = (ArrayNode) type;
                for (int i = 0; i < arrayNode.size(); i++) {
                    type = arrayNode.get(i);
                    String strType = type.asText();
                    if (!strType.equals("null")) return strType;
                }
            }
        }
        return null;
    }

    /**
     * Validate the schema compliancy with some constraints posed by the DPP metadata handling, eg.
     * that a UPI key has been provided, that all the fields listed in the
     * registry.autocomplete.enabled.for list properties actually present in the schema and that all
     * the properties defined in the schema are either of primitive type either of an array of
     * primitive type.
     *
     * @return a List of messages with validation errors, empty if the validation was successful.
     */
    public List<String> validateSchemaCompliancy() {
        debug(LOG, () -> "Validating JSON schema with internal compliancy validation rules...");
        JsonNode schemaNode = schema.getSchemaNode();
        JsonNode properties = schemaNode.get(PROPERTIES_KEY);

        List<String> messages = new ArrayList<>();
        verifyPropertiesTypes(messages, properties);
        verifyAutocompleteEnabledFor(messages, properties);
        verifyUpi(messages, properties);
        verifyReoId(messages, properties);

        return messages;
    }

    private void verifyPropertiesTypes(List<String> messages, JsonNode properties) {
        List<String> invalidProperties = new ArrayList<>();
        properties
                .properties()
                .forEach(
                        entry -> {
                            String propertyName = entry.getKey();
                            JsonNode propertySchema = entry.getValue();
                            if (!isValidPropertyType(propertySchema)) {
                                invalidProperties.add(propertyName);
                            }
                        });
        if (!invalidProperties.isEmpty())
            messages.add("Invalid property types found: " + String.join(", ", invalidProperties));
        debug(LOG, () -> "Find %s issue with property types".formatted(invalidProperties.size()));
    }

    private void verifyReoId(List<String> messages, JsonNode properties) {
        JsonNode reoId = properties.get(config.reoidFieldName());
        if (!nodeIsNotNull(reoId)) {
            debug(LOG, () -> "No property %s found".formatted(config.reoidFieldName()));
            messages.add(
                    "Reo ID property with name %s missing from json schema"
                            .formatted(config.reoidFieldName()));
        }
        JsonNode type = reoId.get(TYPE_KEY);
        if (!nodeIsNotNull(type) && type.isTextual() && !"string".equals(type.asText())) {
            debug(LOG, () -> "Wrong type for property %s found".formatted(config.reoidFieldName()));
            messages.add(
                    "Reo ID property with name %s should be of type string but is defined as type %s"
                            .formatted(config.reoidFieldName(), type.asText()));
        }
    }

    private void verifyUpi(List<String> messages, JsonNode properties) {
        JsonNode upi = properties.get(config.upiFieldName());
        if (!nodeIsNotNull(upi)) {
            debug(LOG, () -> "No property %s found".formatted(config.upiFieldName()));
            messages.add(
                    "UPI property with name %s missing from json schema"
                            .formatted(config.upiFieldName()));
        }
        JsonNode type = upi.get(TYPE_KEY);
        if (!nodeIsNotNull(type) && type.isTextual() && !"string".equals(type.asText())) {
            debug(LOG, () -> "Wrong type found for property %s".formatted(config.upiFieldName()));
            messages.add(
                    "UPI property with name %s should be of type string but is defined as type %s"
                            .formatted(config.upiFieldName(), type.asText()));
        }
    }

    private void verifyAutocompleteEnabledFor(List<String> messages, JsonNode properties) {
        List<String> missingAutocompletes = new ArrayList<>();
        Optional<List<String>> autocompletes = config.autocompletionEnabledFor();
        autocompletes.ifPresent(
                strings ->
                        strings.forEach(
                                p -> {
                                    if (!nodeIsNotNull(properties.get(p)))
                                        missingAutocompletes.add(p);
                                }));
        if (!missingAutocompletes.isEmpty())
            messages.add(
                    "Missing autocomplete properties in schema: "
                            + String.join(", ", missingAutocompletes));
        debug(
                LOG,
                () ->
                        "Found %s issues related to autocomplete properties in schema."
                                .formatted(missingAutocompletes.size()));
    }

    private boolean isValidPropertyType(JsonNode propertySchema) {
        JsonNode typeNode = propertySchema.get("type");
        if (!nodeIsNotNull(typeNode)) return false;
        return !typeIsNotCompliant(typeNode, propertySchema);
    }

    private boolean typeIsNotCompliant(JsonNode type, JsonNode propertySchema) {
        if (type.isTextual()) return !isPrimitiveOrArrayOfPrimitive(type.asText(), propertySchema);
        else if (type instanceof ArrayNode an) {
            for (JsonNode n : an) {
                if (!isPrimitiveOrArrayOfPrimitive(n.asText(), propertySchema)) return true;
            }
            return false;
        } else {
            return true;
        }
    }

    private boolean isPrimitiveOrArrayOfPrimitive(String type, JsonNode propertySchema) {
        return PRIMITIVE_TYPES.contains(type)
                || ("array".equals(type) && isArrayOfPrimitives(propertySchema))
                || "null".equals(type);
    }

    private boolean isArrayOfPrimitives(JsonNode arraySchema) {
        JsonNode items = arraySchema.get(ITEMS_KEY);

        if (items == null) return true;

        if (items.isObject()) {
            JsonNode itemType = items.get(TYPE_KEY);
            if (!nodeIsNotNull(itemType)) return false;

            if (itemType.isTextual()) {
                return PRIMITIVE_TYPES.contains(itemType.asText());
            }

            if (itemType.isArray()) {
                for (JsonNode type : itemType)
                    if (!PRIMITIVE_TYPES.contains(type.asText())) return false;
                return true;
            }
        }

        if (items.isArray()) {
            for (JsonNode itemSchema : items) {
                JsonNode itemType = itemSchema.get(TYPE_KEY);
                if (itemType == null || !PRIMITIVE_TYPES.contains(itemType.asText())) return false;
            }
            return true;
        }
        return false;
    }
}
