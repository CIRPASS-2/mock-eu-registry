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
package it.extrared.registry.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class RoleMapperTest {

    @Inject RoleMapper roleMapper;

    @Test
    @RunOnVertxContext
    public void testRoleMapping1(UniAsserter asserter) {
        AuthenticationRequestContext context = Mockito.mock(AuthenticationRequestContext.class);
        asserter.assertThat(
                () -> roleMapper.augment(buildIdentityWith("ext_admin", "ext_eo"), context),
                si -> {
                    assertEquals(4, si.getRoles().size());
                    assertTrue(si.getRoles().contains(Roles.ADMIN.name()));
                    assertTrue(si.getRoles().contains(Roles.EO.name()));
                });
    }

    @Test
    @RunOnVertxContext
    public void testRoleMapping2(UniAsserter asserter) {
        AuthenticationRequestContext context = Mockito.mock(AuthenticationRequestContext.class);
        asserter.assertThat(
                () -> roleMapper.augment(buildIdentityWith("other_eo"), context),
                si -> {
                    assertEquals(2, si.getRoles().size());
                    assertTrue(si.getRoles().contains(Roles.EO.name()));
                });
    }

    private SecurityIdentity buildIdentityWith(String... roles) {
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();
        for (String r : roles) builder.addRole(r);
        builder.setAnonymous(false);
        builder.setPrincipal(new QuarkusPrincipal("test-user"));
        return builder.build();
    }
}
