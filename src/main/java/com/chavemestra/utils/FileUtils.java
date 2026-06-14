package com.chavemestra.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public final class FileUtils {

    private static final Path VAULT_ROOT = Path.of(System.getProperty("user.home"), ".ChaveMestra");
    private static final Path VAULT_DATA = VAULT_ROOT.resolve("vault");
    private static final Path VAULT_TEMP = VAULT_ROOT.resolve("temp");

    private FileUtils() {
    }

    public static void createVaultDirectories() throws IOException {
        Files.createDirectories(VAULT_DATA);
        Files.createDirectories(VAULT_TEMP);
    }

    public static Path getVaultRoot() {
        return VAULT_ROOT;
    }

    public static Path getVaultDataDirectory() {
        return VAULT_DATA;
    }

    public static Path getVaultTempDirectory() {
        return VAULT_TEMP;
    }

    public static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 0) {
            return "0 B";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        long value = bytes;
        while (value >= 1_048_576 && ci.getIndex() < ci.getEndIndex() - 1) {
            value /= 1024;
            ci.next();
        }
        return String.format("%.1f %cB", value / 1024.0, ci.current());
    }

    public static void openWithDefaultApplication(Path file) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop integration not available on this platform");
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            throw new IOException("File open action not supported on this platform");
        }
        File targetFile = file.toFile();
        if (!targetFile.exists()) {
            throw new IOException("File does not exist: " + file);
        }
        desktop.open(targetFile);
    }

    public static String getFileIcon(String extension) {
        return switch (extension.toLowerCase()) {
            case "pdf" -> "\uD83D\uDCC4";
            case "doc", "docx", "odt", "rtf" -> "\uD83D\uDCC3";
            case "xls", "xlsx", "csv" -> "\uD83D\uDCCA";
            case "ppt", "pptx" -> "\uD83D\uDCCA";
            case "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp" -> "\uD83D\uDDBC";
            case "mp4", "avi", "mkv", "mov", "wmv" -> "\uD83C\uDFAC";
            case "mp3", "wav", "flac", "ogg", "aac" -> "\uD83C\uDFB5";
            case "zip", "rar", "7z", "tar", "gz" -> "\uD83D\uDCE6";
            case "txt", "log", "md" -> "\uD83D\uDCC4";
            case "java", "py", "js", "ts", "html", "css" -> "\uD83D\uDCBB";
            case "exe", "msi", "bat", "sh" -> "⚙";
            case "key", "pem", "cer", "crt" -> "\uD83D\uDD10";
            default -> "\uD83D\uDCC1";
        };
    }
}
