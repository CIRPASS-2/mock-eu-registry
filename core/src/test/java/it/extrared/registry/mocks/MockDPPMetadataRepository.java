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
package it.extrared.registry.mocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.sqlclient.SqlConnection;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.metadata.DPPMetadataRepository;
import it.extrared.registry.utils.CommonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
@Unremovable
public class MockDPPMetadataRepository implements DPPMetadataRepository {
    private static final String METADATA_1 =
                    """
            {
              "registryId":"%s",
              "created_at":"2025/10/20 10:20:33",
              "metadata": {
                "reoId":"12345",
                "upi":"54321",
                "commodityCode": "85176200",
                "dataCarrierTypes":["QR_CODE","RFID"]
              }
            }
            """
                    .formatted(CommonUtils.generateTimeBasedUUID());
    private static final String METADATA_2 =
                    """
            {
              "registryId":"%s",
              "created_at":"2025/10/21 11:30:43",
              "metadata": {
                "reoId":"6789",
                "upi":"6789",
                "commodityCode": "911176200",
                "dataCarrierTypes":["QR_CODE","DATA_MATRIX"]
              }
            }
            """
                    .formatted(CommonUtils.generateTimeBasedUUID());

    @Inject ObjectMapper objectMapper;

    @Override
    public Uni<DPPMetadataEntry> findByUpi(SqlConnection conn, String upi) {
        if (upi.equals("12345")) return uniMeta(METADATA_1);
        else if (upi.equals("6789")) return uniMeta(METADATA_2);
        else return Uni.createFrom().nullItem();
    }

    private Uni<DPPMetadataEntry> uniMeta(String json) {
        return Uni.createFrom()
                .item(
                        Unchecked.supplier(
                                () -> objectMapper.readValue(json, DPPMetadataEntry.class)));
    }

    @Override
    public Uni<DPPMetadataEntry> findBy(SqlConnection conn, List<Tuple2<String, Object>> filters) {
        if (filters == null
                || filters.size() != 1
                || !Objects.equals("reoId", filters.getFirst().getItem1()))
            throw new UnsupportedOperationException(
                    "Mocked implementations supports a single filter for reoId only");
        String reoId = filters.getFirst().getItem2().toString();
        if (Objects.equals("12345", reoId)) return uniMeta(METADATA_1);
        else if (Objects.equals("6789", reoId)) return uniMeta(METADATA_2);
        else return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<DPPMetadataEntry> save(SqlConnection conn, DPPMetadataEntry metadata) {
        metadata.setRegistryId(CommonUtils.generateTimeBasedUUID());
        return Uni.createFrom().item(metadata);
    }

    @Override
    public Uni<DPPMetadataEntry> update(SqlConnection con, DPPMetadataEntry metadata) {
        return Uni.createFrom().item(metadata);
    }
}
