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
package it.extrared.registry;

import io.quarkus.runtime.util.StringUtil;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;
import it.extrared.registry.metadata.update.UpdateType;
import it.extrared.registry.security.Roles;
import it.extrared.registry.utils.MultiMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.spi.Converter;

@ConfigMapping(prefix = "registry")
public interface MetadataRegistryConfig {

    /**
     * @return the default template name in the classpath.
     */
    @WithDefault("default-schema.json")
    String defaultTemplateName();

    /**
     * @return list of JSON fields in a DPP metadata payload for which autocomplete should be
     *     enabled.
     */
    @WithConverter(ListStringConverter.class)
    Optional<List<String>> autocompletionEnabledFor();

    /**
     * @return the update strategy for DPP metadata.
     */
    @WithDefault("MODIFY")
    @WithConverter(UpdateStrategyConverter.class)
    UpdateType updateStrategy();

    /**
     * @return the field name of the unique product identifier in the JSON. Default is upi
     */
    @WithDefault("upi")
    String upiFieldName();

    /**
     * @return the field name of the responsible economic operator identifier in the JSON. Default
     *     is reoId.
     */
    @WithDefault("reoId")
    String reoidFieldName();

    /**
     * @return the field name of the live url in the JSON. Default is liveUrl.
     */
    @WithDefault("liveURL")
    String liveUrlFieldName();

    /**
     * @return true if the registry should try to validate the DPP data associated with the input
     *     metadata entry.
     */
    @WithDefault("false")
    boolean dppValidationEnabled();

    /**
     * @return comma separated list of mappings between external IdP roles and internal roles (see
     *     {@link Roles}) as {ext_rolename}:{internal_roleName} ->
     *     my_ext_role:my_internal_role,my_ext_role2:my_internal_role2
     */
    @WithConverter(RolesMappingsConverter.class)
    @WithDefault("admin:admin,eo:eo,eu:eu")
    MultiMap<String, String> rolesMappings();

    /**
     * @return the location (URI,relative file path or HTTP URL) of a JSON Schema to be loaded.
     */
    Optional<String> jsonSchemaLocation();

    class RolesMappingsConverter implements Converter<MultiMap<String, String>> {

        @Override
        public MultiMap<String, String> convert(String s)
                throws IllegalArgumentException, NullPointerException {
            MultiMap<String, String> map = new MultiMap<>();
            if (StringUtil.isNullOrEmpty(s)) return map;
            String[] mappings = s.split(",");
            Stream.of(mappings)
                    .map(m -> m.split(":"))
                    .filter(arr -> arr.length >= 2)
                    // validates on roles enum
                    .peek(arr -> Roles.valueOf(arr[1].toUpperCase()))
                    .forEach(arr -> map.add(arr[0], arr[1].toUpperCase()));
            return map;
        }
    }

    class UpdateStrategyConverter implements Converter<UpdateType> {

        @Override
        public UpdateType convert(String s) throws IllegalArgumentException, NullPointerException {
            if (StringUtil.isNullOrEmpty(s)) return null;
            String up = s.toUpperCase();
            List<String> strVals = Stream.of(UpdateType.values()).map(Enum::name).toList();
            if (!strVals.contains(up)) {
                throw new IllegalArgumentException(
                        "update-strategy property must be equal to one of this values %s"
                                .formatted(String.join(",", strVals)));
            }
            return UpdateType.valueOf(s.toUpperCase());
        }
    }

    class ListStringConverter implements Converter<List<String>> {

        @Override
        public List<String> convert(String s)
                throws IllegalArgumentException, NullPointerException {
            if (!StringUtil.isNullOrEmpty(s)) return Arrays.asList(s.split(","));
            return Collections.emptyList();
        }
    }
}
