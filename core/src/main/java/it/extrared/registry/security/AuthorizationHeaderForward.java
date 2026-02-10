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

import io.quarkus.rest.client.reactive.ReactiveClientHeadersFactory;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

/** Forwards the authorization header to the outgoing headers of a rest client http request. */
@Provider
public class AuthorizationHeaderForward extends ReactiveClientHeadersFactory {

    private static final String AUTHORIZATION = "Authorization";

    @Override
    public Uni<MultivaluedMap<String, String>> getHeaders(
            MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {
        if (incomingHeaders.containsKey(AUTHORIZATION))
            clientOutgoingHeaders.put(AUTHORIZATION, incomingHeaders.get(AUTHORIZATION));
        return Uni.createFrom().item(clientOutgoingHeaders);
    }
}
