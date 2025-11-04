package org.dzianisbova.domain.load.rpsstrategy;

import java.util.function.DoubleConsumer;

public interface RpsStrategy {
    void setListener(DoubleConsumer listener);

    void start();

    void stop();

    double getStartRps();
}
