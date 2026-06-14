package com.chavemestra.crypto;

public final class CryptoOperationException extends RuntimeException {

    public CryptoOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoOperationException(String message) {
        super(message);
    }
}
