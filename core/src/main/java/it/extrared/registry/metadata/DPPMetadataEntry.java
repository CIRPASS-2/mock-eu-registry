/*
 * Copyright 2025-2026 ExtraRed
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

import static it.extrared.registry.utils.CommonUtils.DATE_TIME_FORMAT;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.Objects;

/** Data class representing a DPPMetadata entry. */
public class DPPMetadataEntry {

    private String registryId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime modifiedAt;

    private JsonNode metadata;

    public DPPMetadataEntry(JsonNode metadata) {
        this.metadata = metadata;
    }

    public DPPMetadataEntry() {}

    public String getRegistryId() {
        return registryId;
    }

    public void setRegistryId(String registryId) {
        this.registryId = registryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        DPPMetadataEntry that = (DPPMetadataEntry) object;
        return Objects.equals(registryId, that.registryId)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(modifiedAt, that.modifiedAt)
                && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryId, createdAt, modifiedAt, metadata);
    }

    @Override
    public String toString() {
        return "DPPMetadataEntry{"
                + "registryId='"
                + registryId
                + '\''
                + ", createdAt="
                + createdAt
                + ", modifiedAt="
                + modifiedAt
                + ", metadata="
                + metadata
                + '}';
    }
}
