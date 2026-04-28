package it.extrared.registry.metadata.update;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlConnection;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.exceptions.InvalidOperationException;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.utils.JsonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class NoneUpdateStrategy implements UpdateStrategy {
    @Inject MetadataRegistryConfig config;

    @Override
    public UpdateType supportedType() {
        return UpdateType.NONE;
    }

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
