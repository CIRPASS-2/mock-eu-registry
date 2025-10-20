package it.extrared.registry.metadata.update;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlConnection;
import it.extrared.registry.metadata.DPPMetadataEntry;

/** Base interface for an Update Strategy. */
public interface UpdateStrategy {

    /**
     * @return the type of updated operation supported by this strategy as an {@link UpdateType}
     */
    UpdateType supportedType();

    /**
     * Perform the actual update of the metadata entry.
     *
     * @param connection a {@link io.vertx.sqlclient.impl.Connection}
     * @param metadata the metadata to update.
     * @return the updated metadata.
     */
    Uni<DPPMetadataEntry> update(SqlConnection connection, DPPMetadataEntry metadata);
}
