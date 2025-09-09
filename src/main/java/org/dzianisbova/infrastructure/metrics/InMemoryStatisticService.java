package org.dzianisbova.infrastructure.metrics;

import org.dzianisbova.domain.metrics.StatisticsService;

public class InMemoryStatisticService implements StatisticsService {
    private long successRequests = 0;
    private long errorRequests = 0;
    private long successDuration = 0;
    private long errorDuration = 0;

    @Override
    public void recordSuccess(long duration) {
        successRequests += 1;
        successDuration += duration;
    }

    @Override
    public void recordError(long duration) {
        errorRequests += 1;
        errorDuration += duration;
    }

    @Override
    public long getTotalRequests() {
        return successRequests + errorRequests;
    }

    @Override
    public long getSuccessCount() {
        return successRequests;
    }

    @Override
    public long getErrorCount() {
        return errorRequests;
    }

    @Override
    public long getAverageSuccessDuration() {
        return successRequests == 0 ? 0 : successDuration / successRequests;
    }

    @Override
    public double getSuccessPercent() {
        long total = errorRequests + successRequests;
        return total == 0 ? 0.0 : (double) successRequests / total;
    }

    @Override
    public void reset() {
        successRequests = 0;
        errorRequests = 0;
        successDuration = 0;
        errorDuration = 0;
    }
}
