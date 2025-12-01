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
package it.extrared.registry.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.SqlConnection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Base interface for a {@link DPPMetadataEntry}. Define methods for the business logic code to
 * interact with the underlying data source.
 */
public interface DPPMetadataRepository {

    Uni<DPPMetadataEntry> findByUpi(SqlConnection conn, String upi);

    Uni<DPPMetadataEntry> findBy(SqlConnection conn, List<Tuple2<String, Object>> filters);

    Uni<DPPMetadataEntry> save(SqlConnection conn, DPPMetadataEntry metadata);

    Uni<DPPMetadataEntry> update(SqlConnection con, DPPMetadataEntry metadata);

    BiFunction<Row, Function<Row, JsonNode>, DPPMetadataEntry> ROW_MAPPER =
            Unchecked.function(
                    (r, f) -> {
                        DPPMetadataEntry metadata = new DPPMetadataEntry();
                        metadata.setRegistryId(r.getString("registry_id"));
                        metadata.setMetadata(f.apply(r));
                        metadata.setCreatedAt(r.getLocalDateTime("created_at"));
                        metadata.setModifiedAt(r.getLocalDateTime("modified_at"));
                        return metadata;
                    });
}
