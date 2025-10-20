package it.extrared.registry.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationMessage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlConnection;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.exceptions.SchemaValidationException;
import it.extrared.registry.jsonschema.SchemaCache;
import it.extrared.registry.metadata.update.DPPMetadataUpdater;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Set;

/** Service class handling create and update operations over DPP metadata. */
@ApplicationScoped
public class DPPMetadataService {

    @Inject DPPMetadataRepository repository;

    @Inject ObjectMapper objectMapper;

    @Inject MetadataRegistryConfig config;

    @Inject DPPMetadataUpdater updater;

    @Inject SchemaCache schemaCache;

    @Inject Pool pool;

    /**
     * Save or update a metadata entry by executing the autocompletion if provided. The way in which
     * data should be updated depends upon the configured {@link
     * MetadataRegistryConfig#updateStrategy()} i.e. property registry.update-strategy.
     *
     * @param metadata the metadata to save/update.
     * @param autocompleteBy the fields to use to retrieve autocompleting values for metadata.
     * @return the saved/updated {@link DPPMetadataEntry}.
     */
    public Uni<DPPMetadataEntry> saveOrUpdate(JsonNode metadata, List<String> autocompleteBy) {
        return pool.withTransaction(c -> saveOrUpdateInternal(c, metadata, autocompleteBy));
    }

    private Uni<DPPMetadataEntry> saveOrUpdateInternal(
            SqlConnection conn, JsonNode metadata, List<String> autocompleteBy) {
        Uni<Void> autocompleted = applyAutoComplete(conn, metadata, autocompleteBy);
        DPPMetadataEntry incoming = new DPPMetadataEntry(metadata);
        return autocompleted
                .flatMap(
                        v ->
                                repository.findByUpi(
                                        conn, metadata.get(config.upiFieldName()).textValue()))
                .flatMap(
                        m -> {
                            if (m != null) return doUpdate(incoming, m, conn);
                            else return doSave(incoming, conn);
                        });
    }

    private Uni<? extends DPPMetadataEntry> doUpdate(
            DPPMetadataEntry modifier, DPPMetadataEntry modified, SqlConnection conn) {
        modified.setMetadata(
                new JsonMerger()
                        .merge(
                                (ObjectNode) modified.getMetadata(),
                                (ObjectNode) modifier.getMetadata()));
        return validate(modified)
                .flatMap(v -> updater.applyUpdate(config.updateStrategy(), conn, modified));
    }

    private Uni<? extends DPPMetadataEntry> doSave(
            DPPMetadataEntry incoming, SqlConnection connection) {
        return validate(incoming).flatMap(v -> repository.save(connection, incoming));
    }

    private Uni<Void> applyAutoComplete(
            SqlConnection con, JsonNode metadata, List<String> autocompleteBy) {
        if (autocompleteBy != null
                && !autocompleteBy.isEmpty()
                && config.autocompletionEnabledFor().isPresent()) {
            List<Tuple2<String, Object>> filters =
                    autocompleteBy.stream()
                            .map(
                                    p ->
                                            Tuple2.of(
                                                    p,
                                                    objectMapper.convertValue(
                                                            metadata.get(p), Object.class)))
                            .toList();
            ObjectNode ometa = (ObjectNode) metadata;
            AutoCompleter autoCompleter =
                    new AutoCompleter(config.autocompletionEnabledFor().get());
            Uni<DPPMetadataEntry> dppMetadata = repository.findBy(con, filters);
            return dppMetadata
                    .invoke(
                            m -> {
                                if (m != null)
                                    autoCompleter.autocomplete(ometa, (ObjectNode) m.getMetadata());
                            })
                    .replaceWithVoid();
        } else {
            return Uni.createFrom().voidItem();
        }
    }

    private Uni<Void> validate(DPPMetadataEntry metadata) {
        return schemaCache
                .get()
                .invoke(
                        s -> {
                            Set<ValidationMessage> msgs = s.validateJson(metadata.getMetadata());
                            if (!msgs.isEmpty()) throw new SchemaValidationException(msgs);
                        })
                .replaceWithVoid();
    }
}
