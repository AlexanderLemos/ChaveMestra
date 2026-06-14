package com.chavemestra.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public final class AESService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int BUFFER_SIZE = 8192;

    private final SecureRandom secureRandom;

    public AESService() {
        this.secureRandom = new SecureRandom();
    }

    public byte[] encrypt(InputStream plaintext, OutputStream ciphertext, SecretKey key) {
        try {
            byte[] iv = generateIV();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = plaintext.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    ciphertext.write(output);
                }
            }
            byte[] finalBlock = cipher.doFinal();
            if (finalBlock != null) {
                ciphertext.write(finalBlock);
            }
            ciphertext.flush();
            return iv;
        } catch (GeneralSecurityException | IOException e) {
            throw new CryptoOperationException("Encryption failed", e);
        }
    }

    public void decrypt(InputStream ciphertext, OutputStream plaintext, SecretKey key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            byte[] inputBytes = ciphertext.readAllBytes();
            byte[] decrypted = cipher.doFinal(inputBytes);
            plaintext.write(decrypted);
            plaintext.flush();
        } catch (GeneralSecurityException | IOException e) {
            throw new CryptoOperationException("Decryption failed — file may be corrupted or password incorrect", e);
        }
    }

    private byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);
        return iv;
    }
}
