package org.dzianisbova.domain.metrics;

import java.time.Duration;

public class ThreadStat {
    private long success = 0;
    private long errors = 0;
    private long totalDuration = 0;
    private long minDuration = Long.MAX_VALUE;
    private long maxDuration = 0;

    public long getSuccess() {
        return success;
    }

    public long getErrors() {
        return errors;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public long getMinDuration() {
        return minDuration;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public void recordSuccess(Duration duration) {
        long millis = duration.toMillis();
        success++;
        totalDuration += millis;
        updateMinMax(millis);
    }

    public void recordError() {
        errors++;
    }

    private void updateMinMax(long millis) {
        minDuration = Math.min(minDuration, millis);
        maxDuration = Math.max(maxDuration, millis);
    }

    public void resetCounters() {
        this.success = 0;
        this.errors = 0;
        this.totalDuration = 0;
        this.minDuration = Long.MAX_VALUE;
        this.maxDuration = 0;
    }
}
