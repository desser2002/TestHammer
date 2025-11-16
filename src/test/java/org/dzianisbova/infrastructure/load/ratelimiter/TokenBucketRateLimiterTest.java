package org.dzianisbova.infrastructure.load.ratelimiter;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketRateLimiterTest {

    @Test
    void acquire_unblocksAfterTokensPerSecondUpdate() throws InterruptedException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(0.0, 1);
        limiter.acquire();

        CountDownLatch waitingStartedLatch = new CountDownLatch(1);
        CountDownLatch acquireFinishedLatch = new CountDownLatch(1);

        Thread tokenAcquirer = startThreadAcquiringToken(limiter, waitingStartedLatch, acquireFinishedLatch);

        waitingStartedLatch.await();
        limiter.setTokensPerSecond(10.0);

        boolean wasUnlocked = acquireFinishedLatch.await(3, TimeUnit.SECONDS);
        assertTrue(wasUnlocked, "acquire() should unlock after tokensPerSecond is updated");

        tokenAcquirer.join();
    }

    private Thread startThreadAcquiringToken(TokenBucketRateLimiter limiter,
                                             CountDownLatch waitingStartedLatch,
                                             CountDownLatch acquireFinishedLatch) {
        Thread thread = new Thread(() -> {
            try {
                waitingStartedLatch.countDown();
                limiter.acquire();
                acquireFinishedLatch.countDown();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
        return thread;
    }
}