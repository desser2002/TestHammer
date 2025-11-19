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
            service.refreshSnapshot();
            long total = service.getTotalRequests();
            long success = service.getSuccessCount();
            long errors = service.getErrorCount();
            double durationAvg = service.getAverageSuccessDuration();
            long minDuration = service.getMinSuccessDurationMillis();
            String minDurationStr = (minDuration == -1) ? "N/A" : Long.toString(minDuration);
            long maxDuration = service.getMaxSuccessDurationMillis();
            String maxDurationStr = (maxDuration == -1) ? "N/A" : Long.toString(maxDuration);
            long p50 = service.getP50();
            long p90 = service.getP90();
            long p95 = service.getP95();
            long p99 = service.getP99();


            CONSOLE.info("Report: Total={}, Success={}, Errors={}, Avg Duration = {} ms, Min Duration = {} ms, Max Duration = {} ms" +
                            ", p50 = {} ms, p90 = {} ms, p95 = {} ms, p99 = {} ms",
                    total, success, errors,
                    String.format("%.3f", durationAvg),
                    minDurationStr,
                    maxDurationStr,
                    p50, p90, p95, p99
            );
        }, 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stopReporting() {
        scheduler.shutdownNow();
    }
}
