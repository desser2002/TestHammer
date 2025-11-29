package org.dzianisbova.domain.api;

import org.dzianisbova.domain.load.RequestExecutor;

import java.util.List;

public class LinearScenario implements Scenario {
    private final List<Request> requests;

    public LinearScenario(List<Request> requests) {
        this.requests = requests;
    }

    @Override
    public void run(RequestExecutor executor) {
        for (Request request : requests) {
            executor.execute(request);
        }
    }

    @Override
    public int getRequestCount() {
        return requests.size();
    }
}
