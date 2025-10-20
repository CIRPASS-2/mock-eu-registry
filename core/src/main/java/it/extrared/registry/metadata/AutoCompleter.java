package it.extrared.registry.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.extrared.registry.MetadataRegistryConfig;
import java.util.Iterator;
import java.util.List;

/** Class responsible to apply an autocomplete to a metadata JSON from and existing one. */
public class AutoCompleter {

    private final List<String> autocompleteFields;

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
        Iterator<String> keys = overlay.fieldNames();
        while (keys.hasNext()) {
            String k = keys.next();
            if (autocompleteFields.contains(k)) {
                JsonNode val = base.get(k);
                if (val.isNull() || val.isMissingNode()) base.set(k, overlay.get(k));
            }
        }
    }
}
