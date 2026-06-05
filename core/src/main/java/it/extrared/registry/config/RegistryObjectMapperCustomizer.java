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
package it.extrared.registry.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.jackson.ObjectMapperCustomizer;

/**
 * Customizes the application-wide Jackson {@link ObjectMapper} by registering the {@link
 * JavaTimeModule}, enabling serialization and deserialization of Java 8 date/time types (e.g.,
 * {@link java.time.LocalDateTime}).
 */
public class RegistryObjectMapperCustomizer implements ObjectMapperCustomizer {

    /**
     * {@inheritDoc}
     *
     * <p>Registers {@link JavaTimeModule} so that {@link java.time.LocalDateTime} and related types
     * are serialized as ISO-8601 strings rather than numeric arrays.
     */
    @Override
    public void customize(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
    }
}
