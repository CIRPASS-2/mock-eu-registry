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
     * @return the field name of the unique product identifier in the JSON.
     */
    @WithDefault("upi")
    String upiFieldName();

    /**
     * @return comma separated list of mappings from external IdP roles and internal roles (see
     *     {@link Roles}) as {ext_rolename}:{internal_roleName} ->
     *     my_ext_role:my_internal_role,my_ext_role2:my_internal_role2
     */
    @WithConverter(RolesMappingsConverter.class)
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
