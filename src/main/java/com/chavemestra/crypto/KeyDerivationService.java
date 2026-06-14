package com.chavemestra.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public final class KeyDerivationService {

    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int DEFAULT_ITERATIONS = 600_000;

    private final SecureRandom secureRandom;

    public KeyDerivationService() {
        this.secureRandom = new SecureRandom();
    }

    public SecretKey deriveKey(char[] password, byte[] salt, int iterations) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGORITHM);
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
            try {
                byte[] keyBytes = factory.generateSecret(spec).getEncoded();
                try {
                    return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
                } finally {
                    Arrays.fill(keyBytes, (byte) 0);
                }
            } finally {
                spec.clearPassword();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptoOperationException("Key derivation failed", e);
        }
    }

    public SecretKey deriveKey(char[] password, byte[] salt) {
        return deriveKey(password, salt, DEFAULT_ITERATIONS);
    }

    public byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        return salt;
    }

    public int getDefaultIterations() {
        return DEFAULT_ITERATIONS;
    }
}
