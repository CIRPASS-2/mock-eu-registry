package it.extrared.registry.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import it.extrared.registry.metadata.DPPMetadataEntry;
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
    public static JsonNode getJsonNode(ObjectMapper objectMapper, Iterator<JsonObject> rs) {
        if (rs.hasNext()) return objectMapper.convertValue(rs.next().getMap(), JsonNode.class);
        return null;
    }
}
