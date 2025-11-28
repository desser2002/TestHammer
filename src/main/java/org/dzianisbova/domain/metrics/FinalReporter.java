package org.dzianisbova.domain.metrics;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FinalReporter {
    private final List<StatsSnapshot> finalReport = new ArrayList<>();
    private final StatisticsService statisticsService;
    private ScheduledExecutorService executor;

    public FinalReporter(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    public void startLogging(Duration snapshotInterval) {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            statisticsService.refreshSnapshot();
            StatsSnapshot snapshot = statisticsService.getStatsSnapshot().get();
            if (snapshot != null) {
                finalReport.add(snapshot);
            }
        }, 0, snapshotInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void reset() {
        finalReport.clear();
    }

    public void stopLogging() {
        executor.shutdownNow();
    }

    public List<StatsSnapshot> getFinalReport() {
        return finalReport;
    }
}
