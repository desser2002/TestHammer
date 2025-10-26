package org.dzianisbova.domain.metrics;

public record StatsSnapshot(long totalSuccess, long totalErrors, long totalDuration, long minDuration,
                            long maxDuration) {

}
