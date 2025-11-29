package org.dzianisbova.domain.metrics;

import java.time.Duration;

public interface StatisticsService {
    void recordSuccess(Duration duration);

    void recordError(Duration duration);

    void reset();
}