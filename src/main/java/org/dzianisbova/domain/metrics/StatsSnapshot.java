package org.dzianisbova.domain.metrics;

public record StatsSnapshot(long totalSuccess, long totalErrors, long totalDuration, long minDuration,
                            long maxDuration, long p50, long p90, long p95, long p99, java.time.Instant creationTime) {

    @Override
    public String toString() {
        long total = totalSuccess + totalErrors;
        double avgDuration = totalSuccess > 0 ? (double) totalDuration / totalSuccess : 0;
        String minDurationStr = (minDuration == Long.MAX_VALUE) ? "N/A" : Long.toString(minDuration);
        String maxDurationStr = (maxDuration == 0) ? "N/A" : Long.toString(maxDuration);

        return String.format(
                "Total=%d, Success=%d, Errors=%d, Avg Duration = %.3f ms, Min Duration = %s ms, Max Duration = %s ms, p50 = %d ms, p90 = %d ms, p95 = %d ms, p99 = %d ms",
                total, totalSuccess, totalErrors, avgDuration, minDurationStr, maxDurationStr, p50, p90, p95, p99
        );
    }

}
