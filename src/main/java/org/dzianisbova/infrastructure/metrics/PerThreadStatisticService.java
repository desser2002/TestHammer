package org.dzianisbova.infrastructure.metrics;

import org.dzianisbova.domain.metrics.StatisticsService;
import org.dzianisbova.domain.metrics.StatsSnapshot;
import org.dzianisbova.domain.metrics.ThreadStat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class PerThreadStatisticService implements StatisticsService {
    private final List<ThreadStat> allStats = new CopyOnWriteArrayList<>();
    private final AtomicReference<StatsSnapshot> statsSnapshot = new AtomicReference<>(null);
    private final ThreadLocal<ThreadStat> threadStat = ThreadLocal.withInitial(() -> {
        ThreadStat stat = new ThreadStat();
        allStats.add(stat);
        return stat;
    });

    @Override
    public void recordSuccess(Duration duration) {
        threadStat.get().recordSuccess(duration);
    }

    @Override
    public void recordError(Duration duration) {
        threadStat.get().recordError();
    }

    @Override
    public AtomicReference<StatsSnapshot> getStatsSnapshot() {
        return statsSnapshot;
    }

    @Override
    public long getTotalRequests() {
        StatsSnapshot snapshot = statsSnapshot.get();
        if (snapshot == null) return 0;
        return snapshot.totalSuccess() + snapshot.totalErrors();
    }

    @Override
    public long getSuccessCount() {
        StatsSnapshot snapshot = statsSnapshot.get();
        if (snapshot == null) return 0;
        return snapshot.totalSuccess();
    }

    @Override
    public long getErrorCount() {
        StatsSnapshot snapshot = statsSnapshot.get();
        if (snapshot == null) return 0;
        return snapshot.totalErrors();
    }

    @Override
    public double getAverageSuccessDuration() {
        StatsSnapshot snapshot = statsSnapshot.get();
        if (snapshot == null || snapshot.totalSuccess() == 0) return 0;
        return (double) snapshot.totalDuration() / snapshot.totalSuccess();
    }

    @Override
    public long getMinSuccessDurationMillis() {
        StatsSnapshot snapshot = statsSnapshot.get();
        if (snapshot == null) return -1;
        long val = snapshot.minDuration();
        return val == Long.MAX_VALUE ? -1 : val;
    }

    @Override
    public long getMaxSuccessDurationMillis() {
        StatsSnapshot snapshot = statsSnapshot.get();
        if (snapshot == null) return -1;
        long val = snapshot.maxDuration();
        return val == 0 ? -1 : val;
    }

    @Override
    public long getP50() {
        StatsSnapshot snapshot = statsSnapshot.get();
        if (snapshot == null) return -1;
        long val = snapshot.p50();
        return val == 0 ? -1 : val;
    }

    @Override
    public long getP90() {
        StatsSnapshot snapshot = statsSnapshot.get();
        if (snapshot == null) return -1;
        long val = snapshot.p90();
        return val == 0 ? -1 : val;
    }

    @Override
    public long getP95() {
        StatsSnapshot snapshot = statsSnapshot.get();
        if (snapshot == null) return -1;
        long val = snapshot.p95();
        return val == 0 ? -1 : val;
    }

    @Override
    public long getP99() {
        StatsSnapshot snapshot = statsSnapshot.get();
        if (snapshot == null) return -1;
        long val = snapshot.p99();
        return val == 0 ? -1 : val;
    }

    public void refreshSnapshot() {
        statsSnapshot.set(aggregate());
    }

    @Override
    public void reset() {
        for (ThreadStat stat : allStats) {
            stat.resetCounters();
        }
        threadStat.remove();
        statsSnapshot.set(null);
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
        long p50 = percentile(successDurations, 50);
        long p90 = percentile(successDurations, 90);
        long p95 = percentile(successDurations, 95);
        long p99 = percentile(successDurations, 99);
        Instant creationTime = Instant.now();
        return new StatsSnapshot(totalSuccess, totalErrors, totalDuration, minDuration, maxDuration, p50, p90, p95, p99, creationTime);
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
