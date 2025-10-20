package it.extrared.registry.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Mean MultiMap for easier use of maps with list values.
 *
 * @param <K>
 * @param <V>
 */
public class MultiMap<K, V> extends HashMap<K, List<V>> {

    /**
     * Adds a single value to the underlying list associated to the provided key.
     *
     * @param key the key of the list
     * @param value the value to add.
     */
    public void add(K key, V value) {
        if (!containsKey(key)) {
            List<V> values = new ArrayList<>();
            put(key, values);
        }
        get(key).add(value);
    }
}
