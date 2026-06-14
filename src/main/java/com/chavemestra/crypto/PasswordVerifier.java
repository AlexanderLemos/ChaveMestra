package com.chavemestra.crypto;

import java.util.Arrays;

public final class PasswordVerifier {

    private final HashService hashService;

    public PasswordVerifier(HashService hashService) {
        this.hashService = hashService;
    }

    public boolean verify(char[] enteredPassword, String storedHash, String storedSalt, int iterations) {
        try {
            HashService.HashedCredential stored = new HashService.HashedCredential(
                    storedHash, storedSalt, iterations);
            return hashService.verifyPassword(enteredPassword, stored);
        } finally {
            Arrays.fill(enteredPassword, '\0');
        }
    }

    public HashService.HashedCredential createCredential(char[] password) {
        try {
            return hashService.hashPassword(password);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
