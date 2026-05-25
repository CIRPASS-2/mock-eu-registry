package it.extrared.registry.metadata.update;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlConnection;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.metadata.DPPMetadataRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Update strategy that creates a new registry entry while preserving the original {@code
 * registryId}. This allows multiple historical records to share the same registry identifier,
 * forming an audit trail of changes for a given product.
 *
 * @see UpdateType#APPEND_WITH_SAME_ID
 */
@ApplicationScoped
public class AppendWithSameIdUpdateStrategy implements UpdateStrategy {

    @Inject DPPMetadataRepository repository;

    /**
     * {@inheritDoc}
     *
     * @return {@link UpdateType#APPEND_WITH_SAME_ID}
     */
    @Override
    public UpdateType supportedType() {
        return UpdateType.APPEND_WITH_SAME_ID;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Inserts {@code dppMetadataEntry} as a new row in the database, keeping its current {@code
     * registryId} unchanged.
     */
    @Override
    public Uni<DPPMetadataEntry> update(
            SqlConnection connection, DPPMetadataEntry dppMetadataEntry) {
        return repository.save(connection, dppMetadataEntry);
    }
}
