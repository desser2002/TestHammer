package org.dzianisbova.infrastructure.load.ratelimiter;

import org.dzianisbova.domain.load.ratelimiter.RateLimiter;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucketRateLimiter implements RateLimiter {
    private final Lock lock = new ReentrantLock();
    private double availableTokens;
    private long lastRefillTimeNanos;
    private double tokensPerSecond;
    private final long capacity;
    private final Condition tokensAvailable = lock.newCondition();
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    public TokenBucketRateLimiter(double tokensPerSecond, long capacity) {
        this.tokensPerSecond = tokensPerSecond;
        this.lastRefillTimeNanos = System.nanoTime();
        this.capacity = capacity;
        this.availableTokens = capacity;
    }

    @Override
    public void acquire() throws InterruptedException {
        lock.lock();
        try {
            refillTokens();
            long nanosToWait = nanosToNextToken();
            while (availableTokens < 1) {
                if (nanosToWait <= 0) {
                    refillTokens();
                    nanosToWait = nanosToNextToken();
                } else {
                    nanosToWait = tokensAvailable.awaitNanos(nanosToWait);
                    refillTokens();
                }
            }
            availableTokens -= 1;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean tryAcquire() {
        lock.lock();
        try {
            refillTokens();
            if (availableTokens >= 1) {
                availableTokens -= 1;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setTokensPerSecond(double newRps) {
        lock.lock();

        try {
            this.tokensPerSecond = newRps;
            tokensAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void refillTokens() {
        long now = System.nanoTime();
        double tokensToAdd = (now - lastRefillTimeNanos) * tokensPerSecond / NANOS_PER_SECOND;
        if (tokensToAdd > 0) {
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
            lastRefillTimeNanos = now;
            tokensAvailable.signalAll();
        }
    }

    private long nanosToNextToken() {
        if (availableTokens >= 1) {
            return 0;
        }
        return (long) ((1 - availableTokens) * NANOS_PER_SECOND / tokensPerSecond);
    }
}
