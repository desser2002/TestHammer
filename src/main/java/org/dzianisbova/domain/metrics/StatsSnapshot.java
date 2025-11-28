package org.dzianisbova.domain.metrics;

public record StatsSnapshot(long totalSuccess, long totalErrors, long totalDuration, long minDuration,
                            long maxDuration, long p50, long p90, long p95, long p99, java.time.Instant creationTime) {

}
