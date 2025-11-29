package org.dzianisbova.domain.api;

import org.dzianisbova.domain.load.RequestExecutor;

public interface Scenario {
    void run(RequestExecutor executor);
    int getRequestCount();
}
