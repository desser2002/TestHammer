package org.dzianisbova.infrastructure.metrics;

import org.dzianisbova.domain.metrics.StatisticReporter;
import org.dzianisbova.domain.metrics.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConsoleStatisticsReporter implements StatisticReporter {
    private static final Logger CONSOLE = LoggerFactory.getLogger(ConsoleStatisticsReporter.class);
    private final StatisticsService service;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ConsoleStatisticsReporter(StatisticsService service) {
        this.service = service;
    }

    @Override
    public void startReporting(long intervalMillis) {
        scheduler.scheduleAtFixedRate(() ->
        {
            long total = service.getTotalRequests();
            long success = service.getSuccessCount();
            long errors = service.getErrorCount();
            double durationAvg = service.getAverageSuccessDuration();
            double successPrecent = service.getSuccessPercent() * 100;

            CONSOLE.info("Report: Total={}, Success={}, Errors={}, Duration = {} ms, Success={}%",
                    total, success, errors,
                    String.format("%.1f", durationAvg),
                    String.format("%.2f", successPrecent));
        }, 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stopReporting() {
        scheduler.shutdownNow();
    }
}
