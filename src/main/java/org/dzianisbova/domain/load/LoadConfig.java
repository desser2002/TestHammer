package org.dzianisbova.domain.load;

import java.time.Duration;

public class LoadConfig {
    private final int threads;
    private final Duration duration;

    private LoadConfig(Builder builder) {
        this.threads = builder.threads;
        this.duration = builder.duration;
    }

    public int getThreadsCount() {
        return threads;
    }

    public Duration getDuration() {
        return duration;
    }

    public static class Builder {
        private int threads = 1;
        private Duration duration = Duration.ofMinutes(1);

        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public LoadConfig build() {
            if (threads <= 0) {
                throw new IllegalStateException("Threads count should be > 0");
            }
            if (duration == null || duration.isZero() || duration.isNegative()) {
                throw new IllegalStateException("Duration should be positive");
            }
            return new LoadConfig(this);
        }
    }
}
