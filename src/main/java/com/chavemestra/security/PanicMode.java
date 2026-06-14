package com.chavemestra.security;

import com.chavemestra.model.AuditEvent;
import com.chavemestra.storage.AuditLogStorage;
import com.chavemestra.storage.FileStorage;
import com.chavemestra.storage.MetadataStorage;
import com.chavemestra.utils.SecureDelete;
import com.chavemestra.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PanicMode {

    private final FileStorage fileStorage;
    private final MetadataStorage metadataStorage;
    private final AuditLogStorage auditLogStorage;

    public PanicMode(FileStorage fileStorage, MetadataStorage metadataStorage,
                     AuditLogStorage auditLogStorage) {
        this.fileStorage = fileStorage;
        this.metadataStorage = metadataStorage;
        this.auditLogStorage = auditLogStorage;
    }

    public PanicResult execute() {
        auditLogStorage.appendEvent(AuditEvent.of(
                AuditEvent.EventType.PANIC_TRIGGERED,
                "Emergency vault destruction initiated after maximum failed attempts exceeded"));

        int filesDestroyed = 0;
        boolean metadataCleared = false;

        try {
            fileStorage.deleteAllEncryptedFiles();
            filesDestroyed = countDestroyedFiles();
        } catch (IOException e) {
            System.err.println("Panic mode: failed to destroy some encrypted files");
        }

        try {
            metadataStorage.deleteAllMetadata();
            metadataCleared = true;
        } catch (IOException e) {
            System.err.println("Panic mode: failed to clear metadata");
        }

        try {
            cleanTempDirectory();
        } catch (IOException e) {
            System.err.println("Panic mode: failed to clean temp directory");
        }

        try {
            auditLogStorage.clearLog();
        } catch (IOException e) {
            System.err.println("Panic mode: failed to clear audit log");
        }

        return new PanicResult(filesDestroyed, metadataCleared);
    }

    private int countDestroyedFiles() throws IOException {
        Path vaultDir = FileUtils.getVaultDataDirectory();
        if (!Files.exists(vaultDir)) {
            return 0;
        }
        int count = 0;
        try (var stream = Files.newDirectoryStream(vaultDir)) {
            for (Path ignored : stream) {
                count++;
            }
        }
        return count;
    }

    private void cleanTempDirectory() throws IOException {
        Path tempDir = FileUtils.getVaultTempDirectory();
        if (Files.exists(tempDir)) {
            try (var stream = Files.newDirectoryStream(tempDir)) {
                for (Path entry : stream) {
                    SecureDelete.secureDelete(entry);
                }
            }
        }
    }

    public record PanicResult(int filesDestroyed, boolean metadataCleared) {
        public boolean isFullyCleared() {
            return metadataCleared;
        }
    }
}
