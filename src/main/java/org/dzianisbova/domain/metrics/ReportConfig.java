package org.dzianisbova.domain.metrics;

public record ReportConfig(long reportIntervalMillis) {
    public ReportConfig {
        if (reportIntervalMillis <= 0) {
            throw new IllegalArgumentException("Report interval must be positive");
        }
    }
}