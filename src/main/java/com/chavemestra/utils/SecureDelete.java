package com.chavemestra.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;

public final class SecureDelete {

    private static final int OVERWRITE_PASSES = 3;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecureDelete() {
    }

    public static void secureDelete(Path file) throws IOException {
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return;
        }
        overwriteFile(file);
        Files.delete(file);
    }

    public static void secureDeleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    secureDeleteDirectory(entry);
                } else {
                    secureDelete(entry);
                }
            }
        }
        Files.delete(directory);
    }

    private static void overwriteFile(Path file) throws IOException {
        long fileSize = Files.size(file);
        if (fileSize == 0) {
            return;
        }

        try (FileChannel channel = FileChannel.open(file,
                StandardOpenOption.WRITE, StandardOpenOption.SYNC)) {
            byte[] randomData = new byte[(int) Math.min(fileSize, 8192)];
            for (int pass = 0; pass < OVERWRITE_PASSES; pass++) {
                channel.position(0);
                long remaining = fileSize;
                while (remaining > 0) {
                    int toWrite = (int) Math.min(remaining, randomData.length);
                    SECURE_RANDOM.nextBytes(randomData);
                    channel.write(ByteBuffer.wrap(randomData, 0, toWrite));
                    remaining -= toWrite;
                }
                channel.force(true);
            }
        }
    }
}
