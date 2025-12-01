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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import it.extrared.registry.TestSupport;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(MetadataAppendUpdatePropertyProfile.class)
public class DPPMetadataServiceAppendTest extends TestSupport {

    private static final String METADATA_UPDATE =
            """
    {
        "reoId":"6789",
        "upi":"6789",
        "commodityCode":"122267310"
    }
    """;

    @Inject ObjectMapper om;
    @Inject DPPMetadataService metadataService;

    @Test
    @RunOnVertxContext
    public void testUpdateWithAutocomplete(UniAsserter asserter) throws JsonProcessingException {
        JsonNode upd = om.readTree(METADATA_UPDATE);
        asserter.assertThat(
                () -> metadataService.saveOrUpdate(upd, List.of("reoId")),
                m -> {
                    assertEquals(
                            upd.get("commodityCode").asText(),
                            m.getMetadata().get("commodityCode").asText());
                    assertEquals(2, m.getMetadata().get("dataCarrierTypes").size());
                    assertNotNull(m.getRegistryId());
                });
    }
}
