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

    long getP50();

    long getP90();

    long getP95();

    long getP99();

    void reset();

    void refreshSnapshot();
}