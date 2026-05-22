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

import static io.quarkus.arc.impl.UncaughtExceptions.LOGGER;
import static it.extrared.registry.utils.CommonUtils.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationMessage;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlConnection;
import it.extrared.registry.MetadataRegistryConfig;
import it.extrared.registry.dpp.DPPFetcher;
import it.extrared.registry.dpp.validation.DPPValidator;
import it.extrared.registry.exceptions.SchemaValidationException;
import it.extrared.registry.jsonschema.SchemaCache;
import it.extrared.registry.metadata.update.DPPMetadataUpdater;
import it.extrared.registry.security.UserAttributesAccessor;
import it.extrared.registry.utils.CommonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/** Service class handling create and update operations over DPP metadata. */
@ApplicationScoped
public class DPPMetadataService {

    @Inject DPPMetadataRepository repository;

    @Inject ObjectMapper objectMapper;

    @Inject MetadataRegistryConfig config;

    @Inject DPPMetadataUpdater updater;

    @Inject SchemaCache schemaCache;

    @Inject DPPValidator dppValidator;

    @Inject UserAttributesAccessor attributesAccessor;

    @Inject DPPFetcher dppFetcher;

    @Inject Pool pool;

    public Uni<String> getProofOfRegistration(String registryId, String reoId) {
        return getByRegistryIdAndReoId(registryId, reoId).map(this::asJWT);
    }

    private String asJWT(DPPMetadataEntry entry) {
        Function<String, String> getFieldName =
                (k) -> {
                    JsonNode node = entry.getMetadata().get(k);
                    if (node != null && !node.isNull() && node.isTextual()) return node.asText();
                    return null;
                };

        return Jwt.claims()
                .claim("registryId", entry.getRegistryId())
                .claim("registeredAt", entry.getCreatedAt().toString())
                .claim("commodityCode", getFieldName.apply(config.commodityCodeFieldName()))
                .claim(
                        "reoId",
                        config.reoidFromClaimEnabled()
                                ? attributesAccessor.getReoId()
                                : getFieldName.apply(config.reoidFieldName()))
                .claim("reoName", attributesAccessor.getReoName())
                .claim("dppHash", entry.getDppHash())
                .claim("dppContentType", entry.getContentType())
                .issuer(config.proofIssuer())
                .jws()
                .keyId(config.keyId())
                .sign();
    }

    public Uni<DPPMetadataEntry> getByRegistryIdAndReoId(String registryId, String reoId) {
        Uni<DPPMetadataEntry> result =
                pool.withConnection(
                        c ->
                                repository.findByRegistryIdAndReoId(
                                        c,
                                        registryId,
                                        config.reoidFromClaimEnabled()
                                                ? attributesAccessor.getReoId()
                                                : reoId));
        return result.invoke(
                m -> {
                    if (m == null)
                        throw new NotFoundException(
                                "No registration found for registryId %s".formatted(registryId));
                });
    }

    /**
     * Save or update a metadata entry by executing the autocompletion if provided. The way in which
     * data should be updated depends upon the configured {@link
     * MetadataRegistryConfig#updateStrategy()} i.e. property registry.update-strategy.
     *
     * @param metadata the metadata to save/update.
     * @param autocompleteBy the fields to use to retrieve autocompleting values for metadata.
     * @return the saved/updated {@link DPPMetadataEntry}.
     */
    public Uni<DPPMetadataEntry> saveOrUpdate(JsonNode metadata, List<String> autocompleteBy) {
        return pool.withTransaction(
                c ->
                        validateUpi(metadata)
                                .flatMap(v -> saveOrUpdateInternal(c, metadata, autocompleteBy)));
    }

    private Uni<Void> validateUpi(JsonNode metadata) {
        return Uni.createFrom()
                .voidItem()
                .invoke(
                        v -> {
                            if (!metadata.has(config.upiFieldName()))
                                throw new SchemaValidationException(
                                        "DPP metadata must declare a %s field"
                                                .formatted(config.upiFieldName()));
                        });
    }

    private Uni<DPPMetadataEntry> saveOrUpdateInternal(
            SqlConnection conn, JsonNode metadata, List<String> autocompleteBy) {
        if (config.reoidFromClaimEnabled()) {
            String reoId = attributesAccessor.getReoId();
            ((ObjectNode) metadata)
                    .set(config.reoidFieldName(), objectMapper.getNodeFactory().textNode(reoId));
        }
        Uni<Void> autocompleted =
                applyAutoComplete(
                        conn,
                        metadata,
                        autocompleteBy != null ? new ArrayList<>(autocompleteBy) : null);
        DPPMetadataEntry incoming = new DPPMetadataEntry(metadata);
        return autocompleted
                .flatMap(
                        v ->
                                repository.findByUpi(
                                        conn, metadata.get(config.upiFieldName()).textValue()))
                .flatMap(
                        m -> {
                            if (m != null) return doUpdate(incoming, m, conn);
                            else return doSave(incoming, conn);
                        });
    }

    private Uni<DPPMetadataEntry> applyDPPValidation(
            DPPMetadataEntry entry, DppWithCType dppWithCType) {
        if (config.dppValidationEnabled()) {
            return dppValidator.validate(entry, dppWithCType);
        } else {
            return Uni.createFrom().item(entry);
        }
    }

    private Uni<? extends DPPMetadataEntry> doUpdate(
            DPPMetadataEntry modifier, DPPMetadataEntry modified, SqlConnection conn) {
        modified.setModifiedAt(LocalDateTime.now());
        modified.setMetadata(
                new JsonMerger()
                        .merge(
                                (ObjectNode) modified.getMetadata(),
                                (ObjectNode) modifier.getMetadata()));
        Uni<DPPMetadataEntry> validated = generateHashAndApplyValidations(modified);
        return validated.flatMap(me -> updater.applyUpdate(config.updateStrategy(), conn, me));
    }

    private Uni<? extends DPPMetadataEntry> doSave(
            DPPMetadataEntry incoming, SqlConnection connection) {
        LocalDateTime createdAt = LocalDateTime.now();
        incoming.setCreatedAt(createdAt);
        incoming.setModifiedAt(createdAt);
        Uni<DPPMetadataEntry> validated = generateHashAndApplyValidations(incoming);
        return validated
                .invoke(m -> m.setRegistryId(CommonUtils.generateTimeBasedUUID()))
                .flatMap(m -> repository.save(connection, m));
    }

    private Uni<Void> applyAutoComplete(
            SqlConnection con, JsonNode metadata, List<String> autocompleteBy) {
        if (autocompleteBy != null
                && !autocompleteBy.isEmpty()
                && config.autocompletionEnabledFor().isPresent()) {
            if (!autocompleteBy.contains(config.reoidFieldName()))
                autocompleteBy.add(config.reoidFieldName());
            List<Tuple2<String, Object>> filters =
                    autocompleteBy.stream()
                            .filter(metadata::has)
                            .map(
                                    p ->
                                            Tuple2.of(
                                                    p,
                                                    objectMapper.convertValue(
                                                            metadata.get(p), Object.class)))
                            .toList();
            ObjectNode ometa = (ObjectNode) metadata;
            AutoCompleter autoCompleter =
                    new AutoCompleter(config.autocompletionEnabledFor().get());
            Uni<DPPMetadataEntry> dppMetadata = repository.findBy(con, filters);
            return dppMetadata
                    .invoke(
                            m -> {
                                if (m != null)
                                    autoCompleter.autocomplete(ometa, (ObjectNode) m.getMetadata());
                            })
                    .replaceWithVoid();
        } else {
            return Uni.createFrom().voidItem();
        }
    }

    private Uni<DPPMetadataEntry> generateHashAndApplyValidations(DPPMetadataEntry entry) {
        Uni<Void> validated = validateMetadata(entry.getMetadata());
        Uni<DppWithCType> dpp = validated.flatMap(v -> getDpp(entry));
        dpp =
                dpp.invoke(
                        dppC -> {
                            entry.setDppHash(sha256(dppC.body()));
                            entry.setContentType(dppC.contentType());
                        });
        return dpp.flatMap(dppC -> applyDPPValidation(entry, dppC));
    }

    private Uni<Void> validateMetadata(JsonNode metadata) {
        return schemaCache
                .get()
                .invoke(
                        s -> {
                            Set<ValidationMessage> msgs = s.validateJson(metadata);
                            if (!msgs.isEmpty()) throw new SchemaValidationException(msgs);
                        })
                .replaceWithVoid();
    }

    private Uni<DppWithCType> getDpp(DPPMetadataEntry entry) {
        return dppFetcher.fetchDPP(getUrl(entry)).map(this::toDto);
    }

    private DppWithCType toDto(HttpResponse<Buffer> response) {
        if (is2xx(response.statusCode())) {
            String cType = response.headers().get("Content-Type");
            byte[] body = response.bodyAsBuffer().getBytes();
            return new DppWithCType(body, cType);
        } else {
            throw new RuntimeException(
                    "Error while retrieving DPP from liveURL. Response Http Status code is %s"
                            .formatted(response.statusCode()));
        }
    }

    private String getUrl(DPPMetadataEntry entry) {
        debug(
                LOGGER,
                () ->
                        "Trying retrieving the live URL using field name %s"
                                .formatted(config.liveUrlFieldName()));
        if (entry.getMetadata() != null && entry.getMetadata().has(config.liveUrlFieldName()))
            return entry.getMetadata().get(config.liveUrlFieldName()).asText();
        return null;
    }
}
