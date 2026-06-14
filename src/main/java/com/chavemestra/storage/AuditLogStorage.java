package com.chavemestra.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.chavemestra.model.AuditEvent;
import com.chavemestra.utils.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class AuditLogStorage {

    private static final String AUDIT_FILE = "audit.log";
    private static final int DEFAULT_LOAD_LIMIT = 500;

    private final ObjectMapper objectMapper;
    private final Path auditFilePath;
    private final Object writeLock = new Object();

    public AuditLogStorage() {
        this.objectMapper = createObjectMapper();
        this.auditFilePath = FileUtils.getVaultRoot().resolve(AUDIT_FILE);
    }

    public void appendEvent(AuditEvent event) {
        synchronized (writeLock) {
            try {
                String json = objectMapper.writeValueAsString(event);
                Files.writeString(auditFilePath, json + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Failed to write audit event: " + e.getMessage());
            }
        }
    }

    public List<AuditEvent> loadEvents() {
        return loadEvents(DEFAULT_LOAD_LIMIT);
    }

    public List<AuditEvent> loadEvents(int limit) {
        List<AuditEvent> events = new ArrayList<>();
        if (!Files.exists(auditFilePath)) {
            return events;
        }
        try (BufferedReader reader = Files.newBufferedReader(auditFilePath, StandardCharsets.UTF_8)) {
            String line;
            List<String> allLines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    allLines.add(line);
                }
            }
            int start = Math.max(0, allLines.size() - limit);
            for (int i = start; i < allLines.size(); i++) {
                try {
                    events.add(objectMapper.readValue(allLines.get(i), AuditEvent.class));
                } catch (IOException ignored) {
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read audit log: " + e.getMessage());
        }
        return events;
    }

    public void clearLog() throws IOException {
        Files.deleteIfExists(auditFilePath);
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
}
