package com.chavemestra.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public final class VaultFile {

    private final String id;
    private final String originalName;
    private final String originalExtension;
    private final String encryptedName;
    private final long fileSizeBytes;
    private final Instant createdAt;
    private final String iv;
    private final String salt;

    @JsonCreator
    public VaultFile(
            @JsonProperty("id") String id,
            @JsonProperty("originalName") String originalName,
            @JsonProperty("originalExtension") String originalExtension,
            @JsonProperty("encryptedName") String encryptedName,
            @JsonProperty("fileSizeBytes") long fileSizeBytes,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("iv") String iv,
            @JsonProperty("salt") String salt) {
        this.id = id;
        this.originalName = originalName;
        this.originalExtension = originalExtension;
        this.encryptedName = encryptedName;
        this.fileSizeBytes = fileSizeBytes;
        this.createdAt = createdAt;
        this.iv = iv;
        this.salt = salt;
    }

    public static VaultFile create(String originalName, String originalExtension,
                                   long fileSizeBytes, String iv, String salt) {
        String id = UUID.randomUUID().toString();
        String encryptedName = "vault_" + id + ".dat";
        return new VaultFile(id, originalName, originalExtension, encryptedName,
                fileSizeBytes, Instant.now(), iv, salt);
    }

    public String getId() {
        return id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getOriginalExtension() {
        return originalExtension;
    }

    public String getEncryptedName() {
        return encryptedName;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getIv() {
        return iv;
    }

    public String getSalt() {
        return salt;
    }
}
