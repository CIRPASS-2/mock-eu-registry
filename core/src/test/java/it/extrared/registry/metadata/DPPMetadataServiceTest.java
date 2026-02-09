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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.extrared.registry.TestSupport;
import it.extrared.registry.exceptions.InvalidDPPException;
import it.extrared.registry.exceptions.SchemaValidationException;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DPPMetadataServiceTest extends TestSupport {

    private static final String METADATA =
            """
    {
        "reoId":"12345",
        "upi":"00001",
        "liveURL":"localhost:1111/dpp"
    }
    """;

    private static final String METADATA_INVALID =
            """
    {
        "reoId":"012345",
        "liveURL":"localhost:1111/dpp"
    }
    """;

    private static final String METADATA_INVALID_2 =
            """
    {
        "upi":"p0012345",
        "reoId":"00000",
        "liveURL":"localhost:1111/dpp"
    }
    """;

    private static final String METADATA_UPDATE =
            """
    {
        "reoId":"6789",
        "upi":"6789",
        "commodityCode":"122267310",
        "liveURL":"localhost:1111/dpp"
    }
    """;

    private static final String METADATA_INVALID_DPP =
            """
    {
        "upi":"012345",
        "reoId":"12345",
        "liveURL":"localhost:2222/dpp"
    }
    """;

    @Inject ObjectMapper om;
    @Inject DPPMetadataService metadataService;

    @Test
    @RunOnVertxContext
    public void testSaveWithAutocomplete(UniAsserter asserter) {
        asserter.assertThat(
                Unchecked.supplier(
                        () ->
                                metadataService.saveOrUpdate(
                                        om.readTree(METADATA), List.of("reoId"))),
                m -> {
                    assertNotNull(m.getMetadata().get("commodityCode"));
                    assertEquals(2, m.getMetadata().get("dataCarrierTypes").size());
                });
    }

    @Test
    @RunOnVertxContext
    public void testValidationFailure(UniAsserter asserter) {
        asserter.assertFailedWith(
                Unchecked.supplier(
                        () ->
                                metadataService.saveOrUpdate(
                                        om.readTree(METADATA_INVALID), List.of("reoId"))),
                t -> {
                    assertEquals(SchemaValidationException.class, t.getClass());
                });
    }

    @Test
    @RunOnVertxContext
    public void testValidationFailure2(UniAsserter asserter) {
        asserter.assertFailedWith(
                Unchecked.supplier(
                        () ->
                                metadataService.saveOrUpdate(
                                        om.readTree(METADATA_INVALID_2), List.of("reoId"))),
                t -> {
                    assertEquals(SchemaValidationException.class, t.getClass());
                });
    }

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
                });
    }

    @Test
    @RunOnVertxContext
    public void testInvalidDPP(UniAsserter asserter) throws JsonProcessingException {
        JsonNode upd = om.readTree(METADATA_INVALID_DPP);
        asserter.assertFailedWith(
                () -> metadataService.saveOrUpdate(upd, List.of("reoId")),
                t -> {
                    assertEquals(InvalidDPPException.class, t.getClass());
                    assertFalse(((InvalidDPPException) t).getValidationReport().isValid());
                });
    }
}
