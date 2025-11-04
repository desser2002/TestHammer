package org.dzianisbova.domain.load.loadphase;

import java.time.Duration;

public class LinearRampUpCalculator implements LoadPhaseCalculator {
    private final LoadPhase loadPhase;

    public LinearRampUpCalculator(LoadPhase loadPhase) {
        this.loadPhase = loadPhase;
    }

    @Override
    public double getCurrentRps(Duration elapsed) {
        if (elapsed.compareTo(loadPhase.getDuration()) >= 0) {
            return loadPhase.getFinalRps();
        }
        double fraction = (double) elapsed.toMillis() / loadPhase.getDuration().toMillis();
        return loadPhase.getStartRps() +
                fraction * (loadPhase.getFinalRps() - loadPhase.getStartRps());
    }

}