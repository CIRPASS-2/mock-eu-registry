package it.extrared.registry.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

/** Data class representing a DPPMetadata entry. */
public class DPPMetadataEntry {

    private String registryId;

    private LocalDateTime createdAt;

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
}
