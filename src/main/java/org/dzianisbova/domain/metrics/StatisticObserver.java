package org.dzianisbova.domain.metrics;

public interface StatisticObserver {
    void onStatisticsUpdated(StatsSnapshot snapshot);
}

