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
package it.extrared.registry.datastore.pgsql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.mutiny.sqlclient.Pool;
import it.extrared.registry.metadata.DPPMetadataEntry;
import it.extrared.registry.metadata.DPPMetadataRepository;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PgSQLDPPMetadataRepositoryTest {

    @Inject Pool pool;

    @Inject ObjectMapper om;

    @Inject DPPMetadataRepository repository;

    @Test
    @RunOnVertxContext
    public void testSave(UniAsserter asserter) throws JsonProcessingException {
        String json =
                """
                {
                    "upi": "urn:epc:id:sgtin:1725242.107346.2025",
                    "reoId": "LEI-529900T8BM49AURSDO55",
                    "commodityCode": "85176211",
                    "dataCarrierTypes": ["QR_CODE", "RFID", "NFC"]
                  }
                """;
        DPPMetadataEntry metadataEntry = new DPPMetadataEntry(om.readTree(json));
        asserter.assertNotNull(
                () ->
                        pool.withTransaction(c -> repository.save(c, metadataEntry))
                                .map(DPPMetadataEntry::getRegistryId));
    }

    @Test
    @RunOnVertxContext
    public void testFindBy(UniAsserter asserter) {
        List<Tuple2<String, Object>> filters =
                List.of(
                        Tuple2.of("commodityCode", "85176200"),
                        Tuple2.of("reoId", "LEI-529900T8BM49AURSDO55"));
        asserter.assertThat(
                () -> pool.withConnection(c -> repository.findBy(c, filters)),
                m -> {
                    assertEquals(
                            "urn:epc:id:sgtin:0614141.107346.2017",
                            m.getMetadata().get("upi").asText());
                });
    }

    @Test
    @RunOnVertxContext
    public void testFindByUpi(UniAsserter asserter) {
        asserter.assertThat(
                () ->
                        pool.withConnection(
                                c ->
                                        repository.findByUpi(
                                                c, "urn:epc:id:sgtin:0614141.107346.2017")),
                m -> {
                    assertEquals(
                            "urn:epc:id:sgtin:0614141.107346.2017",
                            m.getMetadata().get("upi").asText());
                });
    }

    @Test
    @RunOnVertxContext
    public void testUpdate(UniAsserter asserter) throws JsonProcessingException {
        String json =
                """
                {
                    "upi": "urn:epc:id:sgtin:0614141.107346.2017",
                    "reoId": "LEI-529900T8BM49AURSDO55",
                    "commodityCode": "99998888",
                    "dataCarrierTypes": ["QR_CODE", "RFID", "NFC"]
                  }
                """;

        DPPMetadataEntry metadataEntry = new DPPMetadataEntry(om.readTree(json));
        asserter.assertNotNull(
                () ->
                        pool.withTransaction(c -> repository.save(c, metadataEntry))
                                .map(DPPMetadataEntry::getRegistryId));
        asserter.assertEquals(
                () ->
                        pool.withTransaction(c -> repository.update(c, metadataEntry))
                                .map(m -> m.getMetadata().get("commodityCode").asText()),
                "99998888");
    }
}
