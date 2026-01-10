package io.commoncore.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple in-memory rate limiter using token bucket algorithm
 */
public class RateLimiter {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowSizeInSeconds;

    public RateLimiter(int maxRequests, long windowSizeInSeconds) {
        this.maxRequests = maxRequests;
        this.windowSizeInSeconds = windowSizeInSeconds;
    }

    public boolean tryAcquire(String key) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(maxRequests, windowSizeInSeconds));
        return bucket.tryConsume();
    }

    public long getRetryAfterSeconds(String key) {
        TokenBucket bucket = buckets.get(key);
        if (bucket != null) {
            return bucket.getRetryAfterSeconds();
        }
        return 0;
    }

    private static class TokenBucket {
        private final int capacity;
        private final long windowSizeInSeconds;
        private final AtomicInteger tokens;
        private final AtomicLong lastRefillTime;

        public TokenBucket(int capacity, long windowSizeInSeconds) {
            this.capacity = capacity;
            this.windowSizeInSeconds = windowSizeInSeconds;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
        }

        public boolean tryConsume() {
            refill();
            int currentTokens = tokens.get();
            if (currentTokens > 0) {
                return tokens.compareAndSet(currentTokens, currentTokens - 1);
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long lastRefill = lastRefillTime.get();
            long timePassed = (now - lastRefill) / 1000; // seconds

            if (timePassed >= windowSizeInSeconds) {
                if (lastRefillTime.compareAndSet(lastRefill, now)) {
                    tokens.set(capacity);
                }
            }
        }

        public long getRetryAfterSeconds() {
            refill();
            if (tokens.get() > 0) {
                return 0;
            }
            long now = System.currentTimeMillis();
            long lastRefill = lastRefillTime.get();
            long timePassed = (now - lastRefill) / 1000;
            return Math.max(0, windowSizeInSeconds - timePassed);
        }
    }
}
