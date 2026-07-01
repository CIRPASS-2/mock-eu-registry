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
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import it.extrared.registry.TestSupport;
import it.extrared.registry.security.UserAttributesAccessor;
import jakarta.inject.Inject;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(ReoIdFromClaimPropertyProfile.class)
public class DPPMetadataServiceReoIdClaimTest extends TestSupport {

    private static final String METADATA_UPDATE =
            """
    {
                "upi":"12345",
                "commodityCode": "1111111",
                "dataCarrierTypes":["QR_CODE","RFID"]
    }
    """;

    @Inject ObjectMapper om;
    @Inject DPPMetadataService metadataService;
    @InjectMock UserAttributesAccessor accessor;

    @Test
    @RunOnVertxContext
    public void testPreventUpdatesOnNotOwnedDPP(UniAsserter asserter)
            throws JsonProcessingException {
        Mockito.when(accessor.getReoId()).thenReturn("99999");

        JsonNode upd = om.readTree(METADATA_UPDATE);
        asserter.assertFailedWith(
                () -> metadataService.saveOrUpdate(upd, Collections.emptyList()),
                t -> assertTrue(t.getMessage().contains("cannot be updated by '99999'")));
    }
}
