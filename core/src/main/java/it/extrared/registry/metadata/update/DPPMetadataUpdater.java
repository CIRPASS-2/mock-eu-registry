package it.extrared.registry.metadata.update;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlConnection;
import it.extrared.registry.metadata.DPPMetadataEntry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A class that performs the update of a DPP metadata entry by selecting the appropriate strategy.
 */
@ApplicationScoped
public class DPPMetadataUpdater {

    private final Map<UpdateType, UpdateStrategy> strategies;

    @Inject
    public DPPMetadataUpdater(Instance<UpdateStrategy> available) {
        this.strategies = createStrategiesMap(available);
    }

    private Map<UpdateType, UpdateStrategy> createStrategiesMap(
            Instance<UpdateStrategy> available) {
        Iterator<UpdateStrategy> it = available.iterator();
        Map<UpdateType, UpdateStrategy> map = new HashMap<>();
        while (it.hasNext()) {
            UpdateStrategy s = it.next();
            map.put(s.supportedType(), s);
        }
        return map;
    }

    /**
     * Apply the update over a metadata entry.
     *
     * @param updateType the {@link UpdateType} to be used.
     * @param conn a {@link SqlConnection}
     * @param metadata the metadata to payload with the updated data.
     * @return the entry with the data updated.
     */
    public Uni<DPPMetadataEntry> applyUpdate(
            UpdateType updateType, SqlConnection conn, DPPMetadataEntry metadata) {
        UpdateStrategy strategy = strategies.get(updateType);
        if (strategy == null)
            throw new UnsupportedOperationException(
                    "No strategy registered for update type %s".formatted(updateType));
        return strategy.update(conn, metadata);
    }
}
