package org.dzianisbova.infrastructure.metrics;

import org.dzianisbova.domain.metrics.StatisticsService;

import java.time.Duration;

public class InMemoryStatisticService implements StatisticsService {
    private long successRequests = 0;
    private long errorRequests = 0;
    private Duration successDuration = Duration.ZERO;
    private Duration errorDuration = Duration.ZERO;

    @Override
    public void recordSuccess(Duration duration) {
        successRequests += 1;
        successDuration = successDuration.plus(duration);
    }

    @Override
    public void recordError(Duration duration) {
        errorRequests += 1;
        errorDuration = errorDuration.plus(duration);
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
    public double getAverageSuccessDuration() {
        return successRequests == 0 ? 0 : (double) successDuration.toMillis() / successRequests;
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
        successDuration = Duration.ZERO;
        errorDuration = Duration.ZERO;
    }
}
