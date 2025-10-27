package org.dzianisbova.domain.load;

import org.dzianisbova.domain.api.Scenario;
import org.dzianisbova.domain.metrics.ReportConfig;

public interface LoadTestExecutor {
    void executeTest(Scenario scenario, LoadConfig loadConfig, ReportConfig reportConfig);
}