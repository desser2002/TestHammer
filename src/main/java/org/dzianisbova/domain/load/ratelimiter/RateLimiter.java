package org.dzianisbova.domain.load.ratelimiter;

public interface RateLimiter {
    void acquire() throws InterruptedException;
    boolean tryAcquire();
    void setTokensPerSecond(double newRps);
}
