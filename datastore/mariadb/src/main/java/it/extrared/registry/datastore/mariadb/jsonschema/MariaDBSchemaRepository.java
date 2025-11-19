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
package it.extrared.registry.datastore.mariadb.jsonschema;

import static it.extrared.registry.utils.CommonUtils.debug;
import static it.extrared.registry.utils.SQLClientUtils.getJsonNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.utils.StringUtils;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import it.extrared.registry.jsonschema.JsonSchemaDBRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

/** MariaDB implementation of the {@link JsonSchemaDBRepository} */
@ApplicationScoped
public class MariaDBSchemaRepository implements JsonSchemaDBRepository {

    @Inject Pool pool;

    @Inject ObjectMapper objectMapper;

    private static final Logger LOGGER = Logger.getLogger(MariaDBSchemaRepository.class);

    private static final String SELECT_MAX = "SELECT MAX(created_at) FROM json_schemas";

    private static final String SELECT_CURRENT =
                    """
                            SELECT jschema.data_schema
                            FROM json_schemas jschema
                            WHERE created_at = (%s);
            """
                    .formatted(SELECT_MAX);

    private static final String REMOVE_CURRENT =
                    """
                            DELETE
                            FROM json_schemas
                            WHERE created_at = (%s);
            """
                    .formatted(SELECT_MAX);

    private static final String INSERT_SCHEMA =
            """
            INSERT INTO json_schemas (data_schema,created_at)
            VALUES(?,?);
            """;

    @Override
    public Uni<JsonNode> getCurrentJsonSchema() {
        debug(LOGGER, () -> "Retrieving current json schema");
        Uni<RowSet<byte[]>> result =
                pool.query(SELECT_CURRENT)
                        .mapping(
                                r -> {
                                    String json = r.getString(0);
                                    debug(LOGGER, () -> "Current schema is:\n%s".formatted(json));
                                    if (StringUtils.isNotBlank(json)) return json.getBytes();
                                    return null;
                                })
                        .execute();
        return result.map(Unchecked.function(rs -> getJsonNode(objectMapper, rs.iterator())));
    }

    @Override
    public Uni<Void> addSchema(JsonNode schema) {
        try {
            debug(LOGGER, () -> "Persisting a new JSON schema..");
            String rawJson = objectMapper.writeValueAsString(schema);
            debug(LOGGER, () -> "JSON schema is:\n%s".formatted(rawJson));
            return pool.withTransaction(
                            c ->
                                    pool.preparedQuery(INSERT_SCHEMA)
                                            .execute(Tuple.of(rawJson, LocalDateTime.now())))
                    .invoke(rs -> debug(LOGGER, () -> "Insert statement executed"))
                    .replaceWithVoid();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Uni<Void> removeLastSchema() {
        debug(LOGGER, () -> "Removing the last JSON schema added to the repository...");
        return pool.withTransaction(c -> pool.query(REMOVE_CURRENT).execute())
                .invoke(a -> debug(LOGGER, () -> "schema deleted"))
                .replaceWithVoid();
    }
}
