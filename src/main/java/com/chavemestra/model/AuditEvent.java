package com.chavemestra.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record AuditEvent(
        @JsonProperty("timestamp") Instant timestamp,
        @JsonProperty("eventType") EventType eventType,
        @JsonProperty("details") String details) {

    public enum EventType {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        FILE_ENCRYPTED,
        FILE_DECRYPTED,
        FILE_DELETED,
        FILE_OPENED,
        VAULT_LOCKED,
        VAULT_UNLOCKED,
        PANIC_TRIGGERED,
        PASSWORD_GENERATED,
        ACCOUNT_CREATED
    }

    @JsonCreator
    public AuditEvent {
    }

    public static AuditEvent of(EventType eventType, String details) {
        return new AuditEvent(Instant.now(), eventType, details);
    }

    public static AuditEvent of(EventType eventType) {
        return new AuditEvent(Instant.now(), eventType, "");
    }
}
