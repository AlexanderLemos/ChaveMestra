package com.chavemestra.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public final class User {

    private final String passwordHash;
    private final String salt;
    private final int iterations;
    private final Instant createdAt;
    private Instant lastLoginAt;

    @JsonCreator
    public User(
            @JsonProperty("passwordHash") String passwordHash,
            @JsonProperty("salt") String salt,
            @JsonProperty("iterations") int iterations,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("lastLoginAt") Instant lastLoginAt) {
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.iterations = iterations;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public static User create(String passwordHash, String salt, int iterations) {
        Instant now = Instant.now();
        return new User(passwordHash, salt, iterations, now, now);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public int getIterations() {
        return iterations;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void updateLastLogin() {
        this.lastLoginAt = Instant.now();
    }
}
