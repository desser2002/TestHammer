package org.dzianisbova.domain.load.loadphase;

import java.time.Duration;

public interface LoadPhaseCalculator {

    double getCurrentRps(Duration elapsed);
}
