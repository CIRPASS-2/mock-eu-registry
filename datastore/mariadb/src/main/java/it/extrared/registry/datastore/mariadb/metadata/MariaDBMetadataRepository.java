package it.extrared.registry.datastore.mariadb.metadata;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.mutiny.sqlclient.Tuple;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.jsonschema.Schema;
import it.extrared.registry.jsonschema.SchemaCache;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.metadata.DPPMetadataRepository;
import it.extrared.registry.utils.SQLClientUtils;
import it.extrared.registry.utils.UUIDUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MariaDBMetadataRepository implements DPPMetadataRepository {

    @Inject MetadataRegistryConfig config;

    @Inject SchemaCache schemaCache;

    private static final String INSERT =
            """
            INSERT INTO dpp_metadata (registry_id,created_at,modified_at,metadata)
            VALUES($1,$2,$3,$4)
            """;

    private static final String UPDATE =
            """
            UPDATE dpp_metadata SET modified_at=$1, metadata=$2 WHERE
            metadata ->> '$.%s' = $3
            """;

    @Override
    public Uni<DPPMetadataEntry> findByUpi(SqlConnection conn, String upi) {
        String sql =
                        """
                SELECT registry_id,metadata,created_at,modified_at
                FROM dpp_metadata WHERE metadata ->> '%s' = $1 ORDER BY created_at DESC LIMIT 1
                """
                        .formatted(config.upiFieldName());
        Uni<RowSet<DPPMetadataEntry>> rs =
                conn.preparedQuery(sql).mapping(ROW_MAPPER).execute(Tuple.of(upi));
        return rs.map(SQLClientUtils::firstOrNull);
    }

    @Override
    public Uni<DPPMetadataEntry> findBy(SqlConnection conn, List<Tuple2<String, Object>> filters) {
        String sql =
                """
                SELECT registry_id,metadata,created_at,modified_at
                FROM dpp_metadata WHERE %s ORDER BY created_at DESC LIMIT 1
                """;
        List<Object> params = filters.stream().map(Tuple2::getItem2).toList();
        Uni<RowSet<DPPMetadataEntry>> rs =
                schemaCache
                        .get()
                        .map(s -> jsonFilter(filters, s))
                        .flatMap(
                                sf ->
                                        conn.preparedQuery(sql.formatted(sf))
                                                .mapping(ROW_MAPPER)
                                                .execute(Tuple.of(params)));
        return rs.map(SQLClientUtils::firstOrNull);
    }

    @Override
    public Uni<DPPMetadataEntry> save(SqlConnection conn, DPPMetadataEntry metadata) {
        metadata.setRegistryId(UUIDUtils.generateTimeBasedUUID());
        metadata.setCreatedAt(LocalDateTime.now());
        metadata.setModifiedAt(LocalDateTime.now());
        Uni<RowSet<Row>> row =
                conn.preparedQuery(INSERT)
                        .execute(
                                Tuple.of(
                                        metadata.getRegistryId(),
                                        metadata.getCreatedAt(),
                                        metadata.getModifiedAt(),
                                        metadata.getMetadata()));
        return row.map(r -> metadata);
    }

    @Override
    public Uni<DPPMetadataEntry> update(SqlConnection con, DPPMetadataEntry metadata) {
        metadata.setModifiedAt(LocalDateTime.now());
        String upi = metadata.getMetadata().get(config.upiFieldName()).asText();
        Uni<RowSet<Row>> row =
                con.preparedQuery(UPDATE.formatted(config.upiFieldName()))
                        .execute(Tuple.of(metadata.getModifiedAt(), metadata.getMetadata(), upi));
        return row.map(r -> metadata);
    }

    private String jsonFilter(List<Tuple2<String, Object>> filters, Schema schema) {
        List<String> jsonFilters = new ArrayList<>();
        for (int i = 0; i < filters.size(); i++) {
            jsonFilters.add(jsonCondition(filters.get(i), schema, i + 1));
        }
        return String.join(" AND ", jsonFilters);
    }

    private String jsonCondition(Tuple2<String, Object> tuple, Schema schema, int paramIndex) {
        String jproperty = tuple.getItem1();
        String type = schema.getPropertyType(jproperty);
        String pgType = toPgSQLJsonType(type);
        if (pgType.equals("UNSIGNED")) {
            return "CAST(metadata ->> '$.%s' AS UNSIGNED) = $%s".formatted(jproperty, paramIndex);
        }
        return "metadata ->> '$.%s' = $%s".formatted(jproperty, paramIndex);
    }

    private String toPgSQLJsonType(String schemaType) {
        return switch (schemaType) {
            case "string" -> "VARCHAR";
            case "boolean" -> "BOOLEAN";
            case "number", "integer" -> "UNSIGNED";
            default ->
                    throw new UnsupportedOperationException(
                            "Unsupported type %s".formatted(schemaType));
        };
    }
}
