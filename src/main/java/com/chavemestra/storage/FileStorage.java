package com.chavemestra.storage;

import com.chavemestra.crypto.AESService;
import com.chavemestra.crypto.KeyDerivationService;
import com.chavemestra.utils.FileUtils;
import com.chavemestra.utils.SecureDelete;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public final class FileStorage {

    private final AESService aesService;
    private final KeyDerivationService keyDerivationService;
    private final Path vaultDataDirectory;

    public FileStorage(AESService aesService, KeyDerivationService keyDerivationService) {
        this.aesService = aesService;
        this.keyDerivationService = keyDerivationService;
        this.vaultDataDirectory = FileUtils.getVaultDataDirectory();
    }

    public EncryptionResult storeEncryptedFile(Path sourceFile, String encryptedName,
                                                char[] masterPassword) throws IOException {
        byte[] salt = keyDerivationService.generateSalt();
        SecretKey fileKey = keyDerivationService.deriveKey(masterPassword, salt);

        Path encryptedPath = vaultDataDirectory.resolve(encryptedName);

        byte[] iv;
        try (InputStream input = new BufferedInputStream(Files.newInputStream(sourceFile));
             OutputStream output = new BufferedOutputStream(Files.newOutputStream(encryptedPath))) {
            iv = aesService.encrypt(input, output, fileKey);
        }

        return new EncryptionResult(
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(salt));
    }

    public Path retrieveDecryptedFile(String encryptedName, String originalName,
                                       char[] masterPassword, String ivBase64,
                                       String saltBase64) throws IOException {
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        SecretKey fileKey = keyDerivationService.deriveKey(masterPassword, salt);

        Path encryptedPath = vaultDataDirectory.resolve(encryptedName);
        Path tempPath = FileUtils.getVaultTempDirectory().resolve(originalName);

        try (InputStream input = new BufferedInputStream(Files.newInputStream(encryptedPath));
             OutputStream output = new BufferedOutputStream(Files.newOutputStream(tempPath))) {
            aesService.decrypt(input, output, fileKey, iv);
        }

        return tempPath;
    }

    public void deleteEncryptedFile(String encryptedName) throws IOException {
        Path encryptedPath = vaultDataDirectory.resolve(encryptedName);
        SecureDelete.secureDelete(encryptedPath);
    }

    public boolean encryptedFileExists(String encryptedName) {
        return Files.exists(vaultDataDirectory.resolve(encryptedName));
    }

    public void deleteAllEncryptedFiles() throws IOException {
        try (var stream = Files.newDirectoryStream(vaultDataDirectory, "vault_*.dat")) {
            for (Path entry : stream) {
                SecureDelete.secureDelete(entry);
            }
        }
    }

    public record EncryptionResult(String ivBase64, String saltBase64) {
    }
}
