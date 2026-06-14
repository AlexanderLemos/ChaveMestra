package com.chavemestra.controller;

import com.chavemestra.model.AuditEvent;
import com.chavemestra.model.VaultFile;
import com.chavemestra.security.SessionManager;
import com.chavemestra.storage.AuditLogStorage;
import com.chavemestra.storage.FileStorage;
import com.chavemestra.storage.MetadataStorage;
import com.chavemestra.utils.FileUtils;
import com.chavemestra.utils.SecureDelete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class VaultController {

    private static final long TEMP_FILE_CLEANUP_DELAY_SECONDS = 30;
    private static final String PASSWORD_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String PASSWORD_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String PASSWORD_DIGITS = "0123456789";
    private static final String PASSWORD_SYMBOLS = "!@#$%^&*()-_=+[]{}|;:',.<>?/~`";

    private final FileStorage fileStorage;
    private final MetadataStorage metadataStorage;
    private final AuditLogStorage auditLogStorage;
    private final SessionManager sessionManager;
    private final ScheduledExecutorService cleanupScheduler;
    private final SecureRandom secureRandom;

    public VaultController(FileStorage fileStorage, MetadataStorage metadataStorage,
                           AuditLogStorage auditLogStorage, SessionManager sessionManager) {
        this.fileStorage = fileStorage;
        this.metadataStorage = metadataStorage;
        this.auditLogStorage = auditLogStorage;
        this.sessionManager = sessionManager;
        this.cleanupScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "temp-cleanup");
            t.setDaemon(true);
            return t;
        });
        this.secureRandom = new SecureRandom();
    }

    public VaultFile importFile(Path sourceFile) throws IOException {
        if (!sessionManager.isSessionActive()) {
            throw new SecurityException("Session expired. Please re-authenticate.");
        }
        sessionManager.refreshActivity();

        char[] masterPassword = sessionManager.getMasterPassword();
        if (masterPassword == null) {
            throw new SecurityException("No active session");
        }

        try {
            String originalName = sourceFile.getFileName().toString();
            String extension = FileUtils.getFileExtension(originalName);
            long fileSize = Files.size(sourceFile);

            String id = UUID.randomUUID().toString();
            String encryptedName = "vault_" + id + ".dat";

            FileStorage.EncryptionResult result = fileStorage.storeEncryptedFile(
                    sourceFile, encryptedName, masterPassword);

            VaultFile finalFile = new VaultFile(id, originalName, extension, encryptedName,
                    fileSize, Instant.now(), result.ivBase64(), result.saltBase64());

            List<VaultFile> files = metadataStorage.loadVaultFiles();
            files.add(finalFile);
            metadataStorage.saveVaultFiles(files);

            auditLogStorage.appendEvent(AuditEvent.of(
                    AuditEvent.EventType.FILE_ENCRYPTED,
                    "Encrypted and stored: " + originalName));

            return finalFile;
        } finally {
            java.util.Arrays.fill(masterPassword, '\0');
        }
    }

    public Path openFile(VaultFile vaultFile) throws IOException {
        if (!sessionManager.isSessionActive()) {
            throw new SecurityException("Session expired. Please re-authenticate.");
        }
        sessionManager.refreshActivity();

        char[] masterPassword = sessionManager.getMasterPassword();
        if (masterPassword == null) {
            throw new SecurityException("No active session");
        }

        try {
            Path tempFile = fileStorage.retrieveDecryptedFile(
                    vaultFile.getEncryptedName(),
                    vaultFile.getOriginalName(),
                    masterPassword,
                    vaultFile.getIv(),
                    vaultFile.getSalt());

            auditLogStorage.appendEvent(AuditEvent.of(
                    AuditEvent.EventType.FILE_OPENED,
                    "Opened: " + vaultFile.getOriginalName()));

            scheduleCleanup(tempFile);

            FileUtils.openWithDefaultApplication(tempFile);

            return tempFile;
        } finally {
            java.util.Arrays.fill(masterPassword, '\0');
        }
    }

    public void deleteFile(VaultFile vaultFile) throws IOException {
        if (!sessionManager.isSessionActive()) {
            throw new SecurityException("Session expired. Please re-authenticate.");
        }
        sessionManager.refreshActivity();

        fileStorage.deleteEncryptedFile(vaultFile.getEncryptedName());

        List<VaultFile> files = metadataStorage.loadVaultFiles();
        files.removeIf(f -> f.getId().equals(vaultFile.getId()));
        metadataStorage.saveVaultFiles(files);

        auditLogStorage.appendEvent(AuditEvent.of(
                AuditEvent.EventType.FILE_DELETED,
                "Deleted: " + vaultFile.getOriginalName()));
    }

    public List<VaultFile> listFiles() throws IOException {
        return metadataStorage.loadVaultFiles();
    }

    public List<AuditEvent> getAuditLog() {
        return auditLogStorage.loadEvents();
    }

    public String generatePassword(int length, boolean uppercase, boolean lowercase,
                                    boolean digits, boolean symbols) {
        sessionManager.refreshActivity();

        StringBuilder characterPool = new StringBuilder();
        List<String> requiredSets = new ArrayList<>();

        if (uppercase) {
            characterPool.append(PASSWORD_UPPERCASE);
            requiredSets.add(PASSWORD_UPPERCASE);
        }
        if (lowercase) {
            characterPool.append(PASSWORD_LOWERCASE);
            requiredSets.add(PASSWORD_LOWERCASE);
        }
        if (digits) {
            characterPool.append(PASSWORD_DIGITS);
            requiredSets.add(PASSWORD_DIGITS);
        }
        if (symbols) {
            characterPool.append(PASSWORD_SYMBOLS);
            requiredSets.add(PASSWORD_SYMBOLS);
        }

        if (characterPool.isEmpty()) {
            characterPool.append(PASSWORD_UPPERCASE)
                    .append(PASSWORD_LOWERCASE)
                    .append(PASSWORD_DIGITS)
                    .append(PASSWORD_SYMBOLS);
            requiredSets.add(PASSWORD_UPPERCASE);
            requiredSets.add(PASSWORD_LOWERCASE);
            requiredSets.add(PASSWORD_DIGITS);
            requiredSets.add(PASSWORD_SYMBOLS);
        }

        char[] password = new char[length];
        String pool = characterPool.toString();

        int idx = 0;
        for (String required : requiredSets) {
            if (idx < length) {
                password[idx++] = required.charAt(secureRandom.nextInt(required.length()));
            }
        }

        while (idx < length) {
            password[idx++] = pool.charAt(secureRandom.nextInt(pool.length()));
        }

        for (int i = length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }

        auditLogStorage.appendEvent(AuditEvent.of(AuditEvent.EventType.PASSWORD_GENERATED));

        return new String(password);
    }

    public void shutdown() {
        cleanupScheduler.shutdownNow();
        cleanTempDirectory();
    }

    private void scheduleCleanup(Path tempFile) {
        cleanupScheduler.schedule(() -> {
            try {
                SecureDelete.secureDelete(tempFile);
            } catch (IOException e) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }, TEMP_FILE_CLEANUP_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void cleanTempDirectory() {
        try {
            Path tempDir = FileUtils.getVaultTempDirectory();
            if (Files.exists(tempDir)) {
                try (var stream = Files.newDirectoryStream(tempDir)) {
                    for (Path entry : stream) {
                        try {
                            SecureDelete.secureDelete(entry);
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }
}
