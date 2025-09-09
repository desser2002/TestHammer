package org.dzianisbova.domain.metrics;

public interface StatisticReporter {
    void startReporting(long intervalMillis);

    void stopReporting();
}
