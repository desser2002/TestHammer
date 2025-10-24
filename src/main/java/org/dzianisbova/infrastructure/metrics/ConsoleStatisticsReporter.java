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
            if (service instanceof PerThreadStatisticService pts) {
                pts.refreshSnapshot();
            }
            long total = service.getTotalRequests();
            long success = service.getSuccessCount();
            long errors = service.getErrorCount();
            double durationAvg = service.getAverageSuccessDuration();
            long minDuration = service.getMinSuccessDurationMillis();
            String minDurationStr = (minDuration == -1) ? "N/A" : Long.toString(minDuration);
            long maxDuration = service.getMaxSuccessDurationMillis();
            String maxDurationStr = (maxDuration == -1) ? "N/A" : Long.toString(maxDuration);

            CONSOLE.info("Report: Total={}, Success={}, Errors={}, Avg Duration = {} ms, Min Duration = {} ms, Max Duration = {} ms",
                    total, success, errors,
                    String.format("%.3f", durationAvg),
                    minDurationStr,
                    maxDurationStr);
        }, 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stopReporting() {
        scheduler.shutdownNow();
    }
}
