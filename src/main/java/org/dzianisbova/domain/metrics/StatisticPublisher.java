package org.dzianisbova.domain.metrics;

import java.time.Duration;

public interface StatisticPublisher {
    void addObserver(StatisticObserver observer);
    void removeObserver(StatisticObserver observer);
    void start(Duration interval);
    void stop();
}
