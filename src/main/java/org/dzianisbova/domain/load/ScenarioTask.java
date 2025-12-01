package org.dzianisbova.domain.load;

import org.dzianisbova.domain.api.Scenario;

public class ScenarioTask implements Runnable {
    private final Scenario scenario;
    private final RequestExecutor executor;

    public ScenarioTask(Scenario scenario, RequestExecutor executor) {
        this.scenario = scenario;
        this.executor = executor;
    }

    public int getRequestCount() {
        return scenario.getRequestCount();
    }

    @Override
    public void run() {
        scenario.run(executor);
    }
}
