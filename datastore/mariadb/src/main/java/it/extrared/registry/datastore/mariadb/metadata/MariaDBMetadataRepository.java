/*
 * Copyright 2025-2026 ExtraRed
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
package it.extrared.registry.datastore.mariadb.metadata;

import static it.extrared.registry.utils.CommonUtils.debug;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.utils.StringUtils;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.mutiny.sqlclient.Tuple;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.jsonschema.Schema;
import it.extrared.registry.jsonschema.SchemaCache;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.metadata.DPPMetadataRepository;
import it.extrared.registry.utils.CommonUtils;
import it.extrared.registry.utils.JsonUtils;
import it.extrared.registry.utils.SQLClientUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.jboss.logging.Logger;

/** MariaDB implementation of the {@link DPPMetadataRepository} */
@ApplicationScoped
public class MariaDBMetadataRepository implements DPPMetadataRepository {

    @Inject MetadataRegistryConfig config;

    @Inject SchemaCache schemaCache;

    @Inject ObjectMapper objectMapper;

    private static final Logger LOG = Logger.getLogger(MariaDBMetadataRepository.class);

    private static final Function<Row, JsonNode> AS_JSON_META =
            Unchecked.function(
                    r -> {
                        String raw = r.getString("metadata");
                        if (StringUtils.isNotBlank(raw))
                            return JsonUtils.objectMapper().readTree(r.getString("metadata"));
                        else return null;
                    });

    private static final String INSERT =
            """
            INSERT INTO dpp_metadata (registry_id,created_at,modified_at,metadata)
            VALUES(?,?,?,?)
            """;

    private static final String UPDATE =
            """
            UPDATE dpp_metadata SET modified_at=?, metadata=? WHERE
            JSON_VALUE(metadata,'$.%s') = ?
            """;

    @Override
    public Uni<DPPMetadataEntry> findByUpi(SqlConnection conn, String upi) {
        String sql =
                        """
                SELECT registry_id,metadata,created_at,modified_at
                FROM dpp_metadata WHERE JSON_VALUE(metadata,'$.%s') = ? ORDER BY created_at DESC LIMIT 1
                """
                        .formatted(config.upiFieldName());
        debug(LOG, () -> "Executing query %s".formatted(sql));
        Uni<RowSet<DPPMetadataEntry>> rs =
                conn.preparedQuery(sql)
                        .mapping(r -> ROW_MAPPER.apply(r, AS_JSON_META))
                        .execute(Tuple.of(upi));
        return rs.map(SQLClientUtils::firstOrNull)
                .invoke(
                        m ->
                                debug(
                                        LOG,
                                        () ->
                                                "Retrieved metadata by upi %s is %s"
                                                        .formatted(upi, m)));
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
                                                .mapping(r -> ROW_MAPPER.apply(r, AS_JSON_META))
                                                .execute(Tuple.wrap(new ArrayList<>(params))));
        return rs.map(SQLClientUtils::firstOrNull)
                .invoke(m -> debug(LOG, () -> "Retrieved metadata by filters is %s".formatted(m)));
    }

    @Override
    public Uni<DPPMetadataEntry> save(SqlConnection conn, DPPMetadataEntry metadata) {
        try {
            debug(LOG, () -> "Persisting a metadata entry %s".formatted(metadata));
            metadata.setRegistryId(CommonUtils.generateTimeBasedUUID());
            Uni<RowSet<Row>> row =
                    conn.preparedQuery(INSERT)
                            .execute(
                                    Tuple.of(
                                            metadata.getRegistryId(),
                                            metadata.getCreatedAt(),
                                            metadata.getModifiedAt(),
                                            objectMapper.writeValueAsString(
                                                    metadata.getMetadata())));
            return row.map(r -> metadata)
                    .invoke(
                            m ->
                                    debug(
                                            LOG,
                                            () ->
                                                    "Metadata entry %s persisted successfully"
                                                            .formatted(m)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Uni<DPPMetadataEntry> update(SqlConnection con, DPPMetadataEntry metadata) {
        debug(LOG, () -> "Updating metadata entry %s".formatted(metadata));
        metadata.setModifiedAt(LocalDateTime.now());
        String upi = metadata.getMetadata().get(config.upiFieldName()).asText();
        Uni<RowSet<Row>> row =
                con.preparedQuery(UPDATE.formatted(config.upiFieldName()))
                        .execute(Tuple.of(metadata.getModifiedAt(), metadata.getMetadata(), upi));
        return row.map(r -> metadata)
                .invoke(
                        m ->
                                debug(
                                        LOG,
                                        () ->
                                                "Metadata entry %s  persisted successfully"
                                                        .formatted(m)));
    }

    private String jsonFilter(List<Tuple2<String, Object>> filters, Schema schema) {
        List<String> jsonFilters = new ArrayList<>();
        for (Tuple2<String, Object> filter : filters) {
            jsonFilters.add(jsonCondition(filter, schema));
        }
        String queryCondition = String.join(" AND ", jsonFilters);
        debug(LOG, () -> "Result query condition is %s".formatted(queryCondition));
        return queryCondition;
    }

    private String jsonCondition(Tuple2<String, Object> tuple, Schema schema) {
        String jproperty = tuple.getItem1();
        String type = schema.getPropertyType(jproperty);
        String pgType = toPgSQLJsonType(type);
        if (pgType.equals("UNSIGNED")) {
            return "CAST(JSON_VALUE(metadata,'$.%s') AS UNSIGNED) = ?".formatted(jproperty);
        }
        return "JSON_VALUE(metadata,'$.%s') = ?".formatted(jproperty);
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
