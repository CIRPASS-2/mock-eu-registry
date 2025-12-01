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
import it.extrared.registry.metadata.DPPMetadataRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** An implementation of {@link UpdateStrategy} that modify the existing data on the database. */
@ApplicationScoped
public class ModifyUpdateStrategy implements UpdateStrategy {
    @Inject DPPMetadataRepository repository;

    @Override
    public UpdateType supportedType() {
        return UpdateType.MODIFY;
    }

    @Override
    public Uni<DPPMetadataEntry> update(
            SqlConnection connection, DPPMetadataEntry dppMetadataEntry) {
        return repository.update(connection, dppMetadataEntry);
    }
}
