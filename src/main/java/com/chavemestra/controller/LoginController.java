package com.chavemestra.controller;

import com.chavemestra.crypto.HashService;
import com.chavemestra.crypto.PasswordVerifier;
import com.chavemestra.model.AuditEvent;
import com.chavemestra.model.User;
import com.chavemestra.security.AttemptLimiter;
import com.chavemestra.security.PanicMode;
import com.chavemestra.security.SessionManager;
import com.chavemestra.storage.AuditLogStorage;
import com.chavemestra.storage.MetadataStorage;

import java.io.IOException;
import java.util.Arrays;

public final class LoginController {

    private final MetadataStorage metadataStorage;
    private final PasswordVerifier passwordVerifier;
    private final AttemptLimiter attemptLimiter;
    private final SessionManager sessionManager;
    private final PanicMode panicMode;
    private final AuditLogStorage auditLogStorage;

    public LoginController(MetadataStorage metadataStorage, PasswordVerifier passwordVerifier,
                           AttemptLimiter attemptLimiter, SessionManager sessionManager,
                           PanicMode panicMode, AuditLogStorage auditLogStorage) {
        this.metadataStorage = metadataStorage;
        this.passwordVerifier = passwordVerifier;
        this.attemptLimiter = attemptLimiter;
        this.sessionManager = sessionManager;
        this.panicMode = panicMode;
        this.auditLogStorage = auditLogStorage;
    }

    public boolean isFirstLaunch() {
        return !metadataStorage.userExists();
    }

    public RegistrationResult register(char[] password, char[] confirmPassword) {
        if (password.length < 8) {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmPassword, '\0');
            return RegistrationResult.failure("Password must be at least 8 characters");
        }
        if (!Arrays.equals(password, confirmPassword)) {
            Arrays.fill(confirmPassword, '\0');
            Arrays.fill(password, '\0');
            return RegistrationResult.failure("Passwords do not match");
        }
        Arrays.fill(confirmPassword, '\0');

        try {
            char[] passwordCopy = Arrays.copyOf(password, password.length);
            char[] hashCopy = Arrays.copyOf(password, password.length);
            Arrays.fill(password, '\0');
            HashService.HashedCredential credential = passwordVerifier.createCredential(hashCopy);
            User user = User.create(credential.hash(), credential.salt(), credential.iterations());
            metadataStorage.saveUser(user);
            auditLogStorage.appendEvent(AuditEvent.of(AuditEvent.EventType.ACCOUNT_CREATED));
            return RegistrationResult.success(passwordCopy);
        } catch (IOException e) {
            return RegistrationResult.failure("Failed to save credentials: " + e.getMessage());
        }
    }

    public LoginResult login(char[] password, Runnable onSessionExpired) {
        if (attemptLimiter.isLocked()) {
            long remaining = attemptLimiter.getRemainingLockSeconds();
            Arrays.fill(password, '\0');
            return LoginResult.locked(remaining);
        }

        try {
            User user = metadataStorage.loadUser();
            if (user == null) {
                Arrays.fill(password, '\0');
                return LoginResult.failure("No account found. Please register first.", 0);
            }

            char[] sessionCopy = Arrays.copyOf(password, password.length);
            char[] verifyCopy = Arrays.copyOf(password, password.length);
            Arrays.fill(password, '\0');

            boolean valid = passwordVerifier.verify(verifyCopy, user.getPasswordHash(),
                    user.getSalt(), user.getIterations());

            if (valid) {
                attemptLimiter.recordSuccess();
                user.updateLastLogin();
                metadataStorage.saveUser(user);
                sessionManager.startSession(sessionCopy, onSessionExpired);
                Arrays.fill(sessionCopy, '\0');
                auditLogStorage.appendEvent(AuditEvent.of(AuditEvent.EventType.LOGIN_SUCCESS));
                return LoginResult.success();
            } else {
                Arrays.fill(sessionCopy, '\0');
                AttemptLimiter.AttemptResult attemptResult = attemptLimiter.recordFailure();
                auditLogStorage.appendEvent(AuditEvent.of(AuditEvent.EventType.LOGIN_FAILED));

                if (attemptResult == AttemptLimiter.AttemptResult.PANIC_TRIGGERED) {
                    panicMode.execute();
                    return LoginResult.panicTriggered();
                }

                if (attemptResult == AttemptLimiter.AttemptResult.LOCKED) {
                    return LoginResult.locked(attemptLimiter.getRemainingLockSeconds());
                }

                int remaining = attemptLimiter.getRemainingAttempts();
                return LoginResult.failure("Incorrect password", remaining);
            }
        } catch (IOException e) {
            return LoginResult.failure("Authentication error: " + e.getMessage(), 0);
        }
    }

    public void logout() {
        auditLogStorage.appendEvent(AuditEvent.of(AuditEvent.EventType.VAULT_LOCKED));
        sessionManager.destroySession();
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public record LoginResult(Status status, String message, long lockoutSeconds, int remainingAttempts) {

        public enum Status {
            SUCCESS, FAILURE, LOCKED, PANIC
        }

        static LoginResult success() {
            return new LoginResult(Status.SUCCESS, "", 0, 0);
        }

        static LoginResult failure(String message, int remainingAttempts) {
            return new LoginResult(Status.FAILURE, message, 0, remainingAttempts);
        }

        static LoginResult locked(long seconds) {
            return new LoginResult(Status.LOCKED,
                    "Too many failed attempts. Locked for " + seconds + " seconds.", seconds, 0);
        }

        static LoginResult panicTriggered() {
            return new LoginResult(Status.PANIC,
                    "SECURITY ALERT: Maximum attempts exceeded. Vault has been destroyed.", 0, 0);
        }

        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }
    }

    public record RegistrationResult(boolean success, String message, char[] password) {

        static RegistrationResult success(char[] password) {
            return new RegistrationResult(true, "Account created successfully", password);
        }

        static RegistrationResult failure(String message) {
            return new RegistrationResult(false, message, null);
        }
    }
}
