package com.chavemestra.crypto;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public final class HashService {

    private final KeyDerivationService keyDerivationService;

    public HashService(KeyDerivationService keyDerivationService) {
        this.keyDerivationService = keyDerivationService;
    }

    public HashedCredential hashPassword(char[] password) {
        byte[] salt = keyDerivationService.generateSalt();
        int iterations = keyDerivationService.getDefaultIterations();
        byte[] hash = deriveHashBytes(password, salt, iterations);
        try {
            return new HashedCredential(
                    Base64.getEncoder().encodeToString(hash),
                    Base64.getEncoder().encodeToString(salt),
                    iterations);
        } finally {
            Arrays.fill(hash, (byte) 0);
        }
    }

    public boolean verifyPassword(char[] password, HashedCredential stored) {
        byte[] salt = Base64.getDecoder().decode(stored.salt());
        byte[] expectedHash = Base64.getDecoder().decode(stored.hash());
        byte[] actualHash = deriveHashBytes(password, salt, stored.iterations());
        try {
            return constantTimeEquals(expectedHash, actualHash);
        } finally {
            Arrays.fill(actualHash, (byte) 0);
            Arrays.fill(expectedHash, (byte) 0);
            Arrays.fill(salt, (byte) 0);
        }
    }

    private byte[] deriveHashBytes(char[] password, byte[] salt, int iterations) {
        return keyDerivationService.deriveKey(password, salt, iterations).getEncoded();
    }

    private boolean constantTimeEquals(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }

    public record HashedCredential(String hash, String salt, int iterations) {
    }
}
