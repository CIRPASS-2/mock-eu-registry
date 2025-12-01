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
