package com.chavemestra.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.chavemestra.model.User;
import com.chavemestra.model.VaultFile;
import com.chavemestra.utils.FileUtils;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MetadataStorage {

    private static final String USER_FILE = "user.json";
    private static final String METADATA_FILE = "metadata.json";

    private final ObjectMapper objectMapper;
    private final Path storageDirectory;

    public MetadataStorage() {
        this.objectMapper = createObjectMapper();
        this.storageDirectory = FileUtils.getVaultRoot();
    }

    public void saveUser(User user) throws IOException {
        Path target = storageDirectory.resolve(USER_FILE);
        atomicWrite(target, user);
    }

    public User loadUser() throws IOException {
        Path target = storageDirectory.resolve(USER_FILE);
        if (!Files.exists(target)) {
            return null;
        }
        return objectMapper.readValue(target.toFile(), User.class);
    }

    public boolean userExists() {
        return Files.exists(storageDirectory.resolve(USER_FILE));
    }

    public void saveVaultFiles(List<VaultFile> files) throws IOException {
        Path target = storageDirectory.resolve(METADATA_FILE);
        atomicWrite(target, files);
    }

    public List<VaultFile> loadVaultFiles() throws IOException {
        Path target = storageDirectory.resolve(METADATA_FILE);
        if (!Files.exists(target)) {
            return new ArrayList<>();
        }
        VaultFile[] files = objectMapper.readValue(target.toFile(), VaultFile[].class);
        List<VaultFile> result = new ArrayList<>(files.length);
        Collections.addAll(result, files);
        return result;
    }

    public void deleteAllMetadata() throws IOException {
        Files.deleteIfExists(storageDirectory.resolve(USER_FILE));
        Files.deleteIfExists(storageDirectory.resolve(METADATA_FILE));
    }

    private void atomicWrite(Path target, Object data) throws IOException {
        Path tempFile = Files.createTempFile(storageDirectory, "sv_", ".tmp");
        try {
            objectMapper.writeValue(tempFile.toFile(), data);
            try {
                Files.move(tempFile, target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            Files.deleteIfExists(tempFile);
            throw e;
        }
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
