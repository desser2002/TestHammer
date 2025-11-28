package org.dzianisbova.infrastructure.metrics;

import org.dzianisbova.domain.metrics.StatisticObserver;
import org.dzianisbova.domain.metrics.StatisticPublisher;
import org.dzianisbova.domain.metrics.StatisticReporter;
import org.dzianisbova.domain.metrics.StatsSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleStatisticsReporter implements StatisticReporter, StatisticObserver {
    private static final Logger CONSOLE = LoggerFactory.getLogger(ConsoleStatisticsReporter.class);
    private final StatisticPublisher publisher;

    public ConsoleStatisticsReporter(StatisticPublisher publisher) {
        this.publisher = publisher;
    }


    @Override
    public void startReporting(long intervalMillis) {
        publisher.addObserver(this);
    }


    @Override
    public void stopReporting() {
        publisher.removeObserver(this);
    }

    @Override
    public void onStatisticsUpdated(StatsSnapshot snapshot) {
        CONSOLE.info("Report: {}", snapshot);
    }
}
