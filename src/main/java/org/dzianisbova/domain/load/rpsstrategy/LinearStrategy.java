package org.dzianisbova.domain.load.rpsstrategy;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;

public class LinearStrategy implements RpsStrategy {
    private DoubleConsumer listener;
    private final double startRps;
    private final double targetRps;
    private final Duration duration;
    private Instant startTime;
    private ScheduledExecutorService scheduler;
    private static final long RPS_UPDATE_TIME_MILLIS = 100;

    public LinearStrategy(double startRps, double targetRps, Duration duration) {
        this.startRps = startRps;
        this.targetRps = targetRps;
        this.duration = duration;
    }

    @Override
    public double getStartRps() {
        return startRps;
    }

    @Override
    public void setListener(DoubleConsumer listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        this.startTime = Instant.now();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateRps, 0, RPS_UPDATE_TIME_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void updateRps() {
        if (listener == null) return;
        Duration elapsed = Duration.between(startTime, Instant.now());
        double currentRps;
        if (elapsed.compareTo(duration) > 0) {
            currentRps = targetRps;
        } else {
            double progress = (double) elapsed.toMillis() / duration.toMillis();
            currentRps = startRps + (targetRps - startRps) * progress;
        }
        listener.accept(currentRps);
    }
}
