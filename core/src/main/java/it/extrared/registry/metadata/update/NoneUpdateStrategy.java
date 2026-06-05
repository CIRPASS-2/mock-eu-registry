package it.extrared.registry.metadata.update;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlConnection;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.exceptions.InvalidOperationException;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.utils.JsonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Update strategy that rejects any attempt to update an existing registry entry. When a metadata
 * entry for the same UPI is already present, this strategy raises an {@link
 * it.extrared.registry.exceptions.InvalidOperationException} and the registry returns a {@code 400
 * Bad Request} response.
 *
 * @see UpdateType#NONE
 */
@ApplicationScoped
public class NoneUpdateStrategy implements UpdateStrategy {
    @Inject MetadataRegistryConfig config;

    /**
     * {@inheritDoc}
     *
     * @return {@link UpdateType#NONE}
     */
    @Override
    public UpdateType supportedType() {
        return UpdateType.NONE;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Always throws {@link it.extrared.registry.exceptions.InvalidOperationException} to signal
     * that updates are not permitted for the UPI contained in {@code metadata}.
     */
    @Override
    public Uni<DPPMetadataEntry> update(SqlConnection connection, DPPMetadataEntry metadata) {
        String upi = JsonUtils.getJsonFieldAsString(metadata, config.upiFieldName());
        Uni<DPPMetadataEntry> res = Uni.createFrom().nullItem();
        return res.invoke(
                n -> {
                    throw new InvalidOperationException(
                            "DPP registry entry for product with upi %s is already present"
                                    .formatted(upi));
                });
    }
}
