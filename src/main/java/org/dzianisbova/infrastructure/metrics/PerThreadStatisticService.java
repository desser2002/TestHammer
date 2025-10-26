package org.dzianisbova.infrastructure.metrics;

import org.dzianisbova.domain.metrics.StatisticsService;
import org.dzianisbova.domain.metrics.StatsSnapshot;
import org.dzianisbova.domain.metrics.ThreadStat;

import java.time.Duration;
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

    public void refreshSnapshot() {
        statsSnapshot.set(aggregate());
    }

    @Override
    public void reset() {
        allStats.clear();
        threadStat.remove();
        statsSnapshot.set(null);
    }

    private StatsSnapshot aggregate() {
        long totalSuccess = 0;
        long totalErrors = 0;
        long totalDuration = 0;
        long minDuration = Long.MAX_VALUE;
        long maxDuration = 0;

        for (ThreadStat stats : allStats) {
            totalSuccess += stats.getSuccess();
            totalErrors += stats.getErrors();
            totalDuration += stats.getTotalDuration();
            minDuration = Math.min(minDuration, stats.getMinDuration());
            maxDuration = Math.max(maxDuration, stats.getMaxDuration());
        }
        return new StatsSnapshot(totalSuccess, totalErrors, totalDuration, minDuration, maxDuration);
    }
}
