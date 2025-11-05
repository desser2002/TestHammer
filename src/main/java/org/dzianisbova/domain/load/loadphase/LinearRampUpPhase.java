package org.dzianisbova.domain.load.loadphase;

import java.time.Duration;

public class LinearRampUpPhase implements LoadPhase {
    private final double startRps;
    private final double finalRps;
    private final Duration duration;

    public LinearRampUpPhase(double startRps, double finalRps, Duration duration) {
        this.startRps = startRps;
        this.finalRps = finalRps;
        this.duration = duration;
    }

    public double getStartRps() {
        return startRps;
    }

    public double getFinalRps() {
        return finalRps;
    }

    public Duration getDuration() {
        return duration;
    }
}
