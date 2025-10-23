package org.dzianisbova.infrastructure.metrics;

import org.dzianisbova.domain.metrics.StatisticsService;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class InMemoryStatisticService implements StatisticsService {
    private final LongAdder successRequests = new LongAdder();
    private final LongAdder errorRequests = new LongAdder();
    private final LongAdder totalDurationMillis = new LongAdder();

    private final AtomicLong minDurationMillis = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxDurationMillis = new AtomicLong(0);


    @Override
    public void recordSuccess(Duration duration) {
        long millis = duration.toMillis();
        successRequests.increment();
        totalDurationMillis.add(millis);
        updateMinMax(millis);
    }

    @Override
    public void recordError(Duration duration) {
        errorRequests.increment();
    }

    @Override
    public long getTotalRequests() {
        return successRequests.sum() + errorRequests.sum();
    }

    @Override
    public long getSuccessCount() {
        return successRequests.sum();
    }

    @Override
    public long getErrorCount() {
        return errorRequests.sum();
    }

    @Override
    public double getAverageSuccessDuration() {
        long successCount = successRequests.sum();
        return successCount == 0 ? 0 : (double) totalDurationMillis.sum() / successCount;
    }

    @Override
    public long getMinSuccessDurationMillis() {
        long val = minDurationMillis.get();
        return val == Long.MAX_VALUE ? -1 : val;
    }

    @Override
    public long getMaxSuccessDurationMillis() {
        long val = maxDurationMillis.get();
        return val == 0 ? -1 : val;
    }


    @Override
    public void reset() {
        successRequests.reset();
        errorRequests.reset();
        totalDurationMillis.reset();
        minDurationMillis.set(Long.MAX_VALUE);
        maxDurationMillis.set(0);
    }

    private void updateMinMax(long millis) {
        minDurationMillis.getAndUpdate(current -> Math.min(current, millis));
        maxDurationMillis.getAndUpdate(current -> Math.max(current, millis));
    }

}
