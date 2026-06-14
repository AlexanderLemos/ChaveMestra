package com.chavemestra.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class AttemptLimiter {

    private static final int LOCKOUT_THRESHOLD = 5;
    private static final int PANIC_THRESHOLD = 10;
    private static final Duration LOCKOUT_DURATION = Duration.ofSeconds(60);

    private final AtomicInteger failedAttempts = new AtomicInteger(0);
    private final AtomicReference<Instant> lockoutStart = new AtomicReference<>(null);

    public enum AttemptResult {
        ALLOWED,
        LOCKED,
        PANIC_TRIGGERED
    }

    public AttemptResult recordFailure() {
        int attempts = failedAttempts.incrementAndGet();

        if (attempts >= PANIC_THRESHOLD) {
            return AttemptResult.PANIC_TRIGGERED;
        }

        if (attempts >= LOCKOUT_THRESHOLD) {
            lockoutStart.set(Instant.now());
            return AttemptResult.LOCKED;
        }

        return AttemptResult.ALLOWED;
    }

    public void recordSuccess() {
        failedAttempts.set(0);
        lockoutStart.set(null);
    }

    public boolean isLocked() {
        Instant start = lockoutStart.get();
        if (start == null) {
            return false;
        }
        if (Duration.between(start, Instant.now()).compareTo(LOCKOUT_DURATION) >= 0) {
            lockoutStart.set(null);
            return false;
        }
        return true;
    }

    public long getRemainingLockSeconds() {
        Instant start = lockoutStart.get();
        if (start == null) {
            return 0;
        }
        Duration elapsed = Duration.between(start, Instant.now());
        Duration remaining = LOCKOUT_DURATION.minus(elapsed);
        if (remaining.isNegative()) {
            lockoutStart.set(null);
            return 0;
        }
        return remaining.getSeconds();
    }

    public int getFailedAttempts() {
        return failedAttempts.get();
    }

    public int getRemainingAttempts() {
        return Math.max(0, LOCKOUT_THRESHOLD - failedAttempts.get());
    }

    public int getLockoutThreshold() {
        return LOCKOUT_THRESHOLD;
    }

    public int getPanicThreshold() {
        return PANIC_THRESHOLD;
    }
}
