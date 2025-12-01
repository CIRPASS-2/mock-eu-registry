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
package it.extrared.registry.exceptions;

import com.networknt.schema.ValidationMessage;
import java.util.Set;

/**
 * Exception used to signal the failure of a JSON schema validation performed over a JSON document.
 */
public class SchemaValidationException extends RuntimeException {

    public SchemaValidationException(Set<ValidationMessage> messages) {
        super(errorMessage(messages));
    }

    private static String errorMessage(Set<ValidationMessage> messages) {
        String msg =
                String.join(
                        "\n",
                        messages.stream().map(SchemaValidationException::formatError).toList());
        return String.format(
                "Json schema validation returned the following errors: %s".formatted(msg));
    }

    private static String formatError(ValidationMessage vm) {
        return String.format("Path: %s - Error: %s", vm.getProperty(), vm.getMessage());
    }
}
