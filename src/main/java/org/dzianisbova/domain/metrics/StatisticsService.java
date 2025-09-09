package org.dzianisbova.domain.metrics;

public interface StatisticsService {
    void recordSuccess(long duration);

    void recordError(long duration);

    long getTotalRequests();

    long getSuccessCount();

    long getErrorCount();

    long getAverageSuccessDuration();

    double getSuccessPercent();

    void reset();
}
