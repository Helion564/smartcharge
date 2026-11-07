package com.smartcharge.booking.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Distributed locking utility backed by Redis.
 * Uses SET NX (set if not exists) with TTL to ensure only one booking creation
 * proceeds at a time for a given connector, preventing double-booking race conditions.
 *
 * <p>Each lock is associated with a unique token (UUID) so that only the holder
 * of the lock can release it.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisDistributedLock {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_PREFIX = "lock:connector:";

    /**
     * Attempts to acquire a lock for the given connector.
     *
     * @param connectorId the connector to lock
     * @param ttlSeconds  time-to-live for the lock in seconds
     * @return the lock token if acquired, or null if the lock is already held
     */
    public String tryAcquireLock(String connectorId, long ttlSeconds) {
        String lockKey = LOCK_PREFIX + connectorId;
        String token = UUID.randomUUID().toString();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, token, Duration.ofSeconds(ttlSeconds));
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Acquired Redis lock for connector: {} with token: {}", connectorId, token);
            return token;
        }
        log.debug("Failed to acquire Redis lock for connector: {}", connectorId);
        return null;
    }

    /**
     * Releases a lock only if the provided token matches the stored token.
     * This prevents accidental release of a lock held by another caller.
     *
     * @param connectorId the connector to unlock
     * @param token       the token obtained during {@link #tryAcquireLock}
     * @return true if the lock was released, false if it wasn't held by this token
     */
    public boolean releaseLock(String connectorId, String token) {
        String lockKey = LOCK_PREFIX + connectorId;
        String storedToken = stringRedisTemplate.opsForValue().get(lockKey);
        if (token != null && token.equals(storedToken)) {
            Boolean deleted = stringRedisTemplate.delete(lockKey);
            log.debug("Released Redis lock for connector: {}", connectorId);
            return Boolean.TRUE.equals(deleted);
        }
        log.warn("Could not release lock for connector: {} — token mismatch or lock expired", connectorId);
        return false;
    }

    /**
     * Checks whether a lock is currently held for a connector.
     *
     * @param connectorId the connector to check
     * @return true if locked
     */
    public boolean isLocked(String connectorId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOCK_PREFIX + connectorId));
    }

    /**
     * Forces release of a lock regardless of token (admin use only).
     *
     * @param connectorId the connector to forcibly unlock
     */
    public void forceReleaseLock(String connectorId) {
        String lockKey = LOCK_PREFIX + connectorId;
        stringRedisTemplate.delete(lockKey);
        log.warn("Forcibly released lock for connector: {}", connectorId);
    }
}
