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
