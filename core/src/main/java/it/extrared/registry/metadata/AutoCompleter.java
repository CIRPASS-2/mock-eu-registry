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
package it.extrared.registry.metadata;

import static it.extrared.registry.utils.CommonUtils.debug;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.extrared.registry.MetadataRegistryConfig;
import java.util.Iterator;
import java.util.List;
import org.jboss.logging.Logger;

/** Class responsible to apply an autocomplete to a metadata JSON from and existing one. */
public class AutoCompleter {

    private final List<String> autocompleteFields;

    private static final Logger LOG = Logger.getLogger(AutoCompleter.class);

    public AutoCompleter(List<String> autocompleteFields) {
        this.autocompleteFields = autocompleteFields;
    }

    /**
     * Set the fields' values in the overlay JSON to the base JSON if comprised in the list of
     * enabled fields into the property {@link MetadataRegistryConfig#autocompletionEnabledFor()}
     * i.e. registry.autocompletion-enabled-for.
     *
     * @param base the base JSON where autocomplete fields should be set.
     * @param overlay the overlay JSON providing the fields' values to be set in the base JSON.
     */
    public void autocomplete(ObjectNode base, ObjectNode overlay) {
        debug(
                LOG,
                () ->
                        "Performing autocomplete setting missing fields in \n %s from \n %s"
                                .formatted(base, overlay));
        Iterator<String> keys = overlay.fieldNames();
        while (keys.hasNext()) {
            String k = keys.next();
            debug(LOG, () -> "Checking if %s is enabled for autocompletion".formatted(k));
            if (autocompleteFields.contains(k)) {
                debug(
                        LOG,
                        () ->
                                "Performing autocomplete for %s if base node is missing property"
                                        .formatted(k));
                JsonNode val = base.get(k);
                if (val == null || val.isNull() || val.isMissingNode()) base.set(k, overlay.get(k));
            }
        }
    }
}
