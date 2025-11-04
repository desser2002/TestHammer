package org.dzianisbova.domain.load.loadphase;

import java.time.Duration;

public interface LoadPhase {
    double getStartRps();
    double getFinalRps();
    Duration getDuration();
}
