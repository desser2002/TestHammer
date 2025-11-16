package org.dzianisbova.domain.metrics;

import java.time.Duration;

public interface StatisticsService {
    void recordSuccess(Duration duration);

    void recordError(Duration duration);

    long getTotalRequests();

    long getSuccessCount();

    long getErrorCount();

    double getAverageSuccessDuration();

    long getMinSuccessDurationMillis();

    long getMaxSuccessDurationMillis();

    long getPercentile50();

    long getPercentile75();

    long getPercentile95();

    long getPercentile99();

    void reset();

    void refreshSnapshot();
}