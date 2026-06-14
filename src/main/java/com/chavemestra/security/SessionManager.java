package com.chavemestra.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class SessionManager {

    private static final Duration DEFAULT_IDLE_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration CHECK_INTERVAL = Duration.ofSeconds(10);

    private final Duration idleTimeout;
    private final AtomicReference<Instant> lastActivity = new AtomicReference<>(Instant.now());
    private final AtomicReference<char[]> masterPassword = new AtomicReference<>(null);

    private ScheduledExecutorService scheduler;
    private Runnable onSessionExpired;
    private volatile boolean active = false;

    public SessionManager() {
        this(DEFAULT_IDLE_TIMEOUT);
    }

    public SessionManager(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public void startSession(char[] password, Runnable onExpired) {
        destroySession();
        char[] copy = new char[password.length];
        System.arraycopy(password, 0, copy, 0, password.length);
        masterPassword.set(copy);
        lastActivity.set(Instant.now());
        this.onSessionExpired = onExpired;
        this.active = true;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "session-monitor");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::checkExpiration,
                CHECK_INTERVAL.getSeconds(),
                CHECK_INTERVAL.getSeconds(),
                TimeUnit.SECONDS);
    }

    public void refreshActivity() {
        if (active) {
            lastActivity.set(Instant.now());
        }
    }

    public boolean isSessionActive() {
        return active && !isSessionExpired();
    }

    public boolean isSessionExpired() {
        if (!active) {
            return true;
        }
        Instant last = lastActivity.get();
        return Duration.between(last, Instant.now()).compareTo(idleTimeout) >= 0;
    }

    public char[] getMasterPassword() {
        char[] pw = masterPassword.get();
        if (pw == null) {
            return null;
        }
        char[] copy = new char[pw.length];
        System.arraycopy(pw, 0, copy, 0, pw.length);
        return copy;
    }

    public void destroySession() {
        active = false;
        char[] pw = masterPassword.getAndSet(null);
        if (pw != null) {
            java.util.Arrays.fill(pw, '\0');
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    public long getIdleTimeoutSeconds() {
        return idleTimeout.getSeconds();
    }

    public long getSecondsUntilExpiry() {
        Instant last = lastActivity.get();
        Duration elapsed = Duration.between(last, Instant.now());
        Duration remaining = idleTimeout.minus(elapsed);
        return Math.max(0, remaining.getSeconds());
    }

    private void checkExpiration() {
        if (isSessionExpired() && active) {
            active = false;
            char[] pw = masterPassword.getAndSet(null);
            if (pw != null) {
                java.util.Arrays.fill(pw, '\0');
            }
            if (onSessionExpired != null) {
                onSessionExpired.run();
            }
        }
    }
}
