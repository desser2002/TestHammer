package org.dzianisbova.infrastructure.load.ratelimiter;

import org.dzianisbova.domain.load.ratelimiter.RateLimiter;

public class NoOpRateLimiter implements RateLimiter {
    @Override
    public void acquire() {
    }

    @Override
    public boolean tryAcquire() {
        return true;
    }
}