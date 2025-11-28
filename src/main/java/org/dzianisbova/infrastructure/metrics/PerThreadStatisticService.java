package org.dzianisbova.infrastructure.metrics;

import org.dzianisbova.domain.metrics.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PerThreadStatisticService implements StatisticsService, StatisticPublisher {
    private final List<ThreadStat> allStats = new CopyOnWriteArrayList<>();
    private StatsSnapshot statsSnapshot;
    private final ThreadLocal<ThreadStat> threadStat = ThreadLocal.withInitial(() -> {
        ThreadStat stat = new ThreadStat();
        allStats.add(stat);
        return stat;
    });

    private final List<StatisticObserver> statisticObservers = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;

    @Override
    public void recordSuccess(Duration duration) {
        threadStat.get().recordSuccess(duration);
    }

    @Override
    public void recordError(Duration duration) {
        threadStat.get().recordError();
    }

    @Override
    public void reset() {
        for (ThreadStat stat : allStats) {
            stat.resetCounters();
        }
        threadStat.remove();
        statsSnapshot = null;
    }

    @Override
    public void start(Duration interval) {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
                    refreshSnapshot();
                    notifyObservers();
                }
                , 0, interval.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void addObserver(StatisticObserver observer) {
        statisticObservers.add(observer);
    }

    @Override
    public void removeObserver(StatisticObserver observer) {
        statisticObservers.remove(observer);
    }


    private StatsSnapshot aggregate() {
        long totalSuccess = 0;
        long totalErrors = 0;
        long totalDuration = 0;
        long minDuration = Long.MAX_VALUE;
        long maxDuration = 0;
        List<Long> successDurations = new ArrayList<>();

        for (ThreadStat stats : allStats) {
            totalSuccess += stats.getSuccess();
            totalErrors += stats.getErrors();
            totalDuration += stats.getTotalDuration();
            minDuration = Math.min(minDuration, stats.getMinDuration());
            maxDuration = Math.max(maxDuration, stats.getMaxDuration());
            successDurations.addAll(stats.getSuccessDurations());
        }

        successDurations.sort(Comparator.naturalOrder());
        return new StatsSnapshot(
                totalSuccess,
                totalErrors,
                totalDuration,
                minDuration,
                maxDuration,
                percentile(successDurations, 50),
                percentile(successDurations, 90),
                percentile(successDurations, 95),
                percentile(successDurations, 99),
                Instant.now()
        );
    }

    private synchronized void notifyObservers() {
        if (statsSnapshot != null) {
            statisticObservers.forEach(observer -> observer.onStatisticsUpdated(statsSnapshot));
        }
    }

    private synchronized void refreshSnapshot() {
        statsSnapshot = aggregate();
    }

    private static long percentile(List<Long> sortedList, double percentile) {
        if (sortedList.isEmpty()) {
            return 0L;
        }
        int size = sortedList.size();
        int index = (int) Math.ceil(percentile / 100 * size) - 1;
        if (index < 0) index = 0;
        if (index >= size) index = size - 1;
        return sortedList.get(index);
    }
}
