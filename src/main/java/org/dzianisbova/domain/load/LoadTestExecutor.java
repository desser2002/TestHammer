package org.dzianisbova.domain.load;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.metrics.ReportConfig;

public interface LoadTestExecutor {
    void executeTest(Request request, LoadConfig loadConfig, ReportConfig reportConfig);
}