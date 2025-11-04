package org.dzianisbova.domain.load;

import org.dzianisbova.domain.load.rpsstrategy.RpsStrategy;

import java.time.Duration;

public class LoadConfig {
    private final int threads;
    private final Duration testDuration;
    private final Duration warmUpDuration;
    private final RpsStrategy rpsStrategy;

    private LoadConfig(Builder builder) {
        this.threads = builder.threads;
        this.testDuration = builder.testDuration;
        this.warmUpDuration = builder.warmUpDuration;
        this.rpsStrategy = builder.rpsStrategy;
    }

    public int getThreadsCount() {
        return threads;
    }

    public Duration getTestDuration() {
        return testDuration;
    }

    public Duration getWarmUpDuration() {
        return warmUpDuration;
    }

    public RpsStrategy getRpsStrategy() {
        return rpsStrategy;
    }

    public static class Builder {
        private int threads = 1;
        private RpsStrategy rpsStrategy;
        private Duration testDuration = Duration.ofMinutes(1);
        private Duration warmUpDuration = Duration.ZERO;

        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder duration(Duration duration) {
            this.testDuration = duration;
            return this;
        }

        public Builder warmUpDuration(Duration duration) {
            this.warmUpDuration = duration;
            return this;
        }

        public Builder rpsStrategy(RpsStrategy rpsStrategy) {
            this.rpsStrategy = rpsStrategy;
            return this;
        }

        public LoadConfig build() {
            if (threads <= 0) {
                throw new IllegalStateException("Threads count should be > 0");
            }
            if (testDuration == null || testDuration.isZero() || testDuration.isNegative()) {
                throw new IllegalStateException("Duration should be positive");
            }
            if (warmUpDuration == null || warmUpDuration.isNegative()) {
                throw new IllegalArgumentException("Warm up duration should be 0 or more");
            }
            if (rpsStrategy == null) {
                throw new IllegalStateException("RpsStrategy must be set");
            }
            return new LoadConfig(this);
        }
    }
}
