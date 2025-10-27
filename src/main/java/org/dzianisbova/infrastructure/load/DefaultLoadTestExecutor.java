package org.dzianisbova.infrastructure.load;

import org.dzianisbova.domain.api.Scenario;
import org.dzianisbova.domain.load.LoadConfig;
import org.dzianisbova.domain.load.LoadTestExecutor;
import org.dzianisbova.domain.load.RequestExecutor;
import org.dzianisbova.domain.logging.Logger;
import org.dzianisbova.domain.metrics.ReportConfig;
import org.dzianisbova.domain.metrics.StatisticReporter;
import org.dzianisbova.domain.metrics.StatisticsService;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultLoadTestExecutor implements LoadTestExecutor {
    private final StatisticsService statisticsService;
    private final StatisticReporter statisticReporter;
    private final Logger logger;

    private ExecutorService executorService;
    private RequestExecutor requestExecutor;

    public DefaultLoadTestExecutor(StatisticsService statisticsService, StatisticReporter statisticReporter, Logger logger) {
        this.statisticsService = statisticsService;
        this.statisticReporter = statisticReporter;
        this.logger = logger;
    }

    private void initializeRequestExecutors(LoadConfig loadConfig) {
        HttpClient httpClient;
        httpClient = HttpClient.newHttpClient();
        executorService = Executors.newFixedThreadPool(loadConfig.getThreadsCount());
        requestExecutor = new ConcurrentRequestExecutor(
                logger,
                httpClient,
                statisticsService);
    }

    @Override
    public void executeTest(Scenario scenario, LoadConfig loadConfig, ReportConfig reportConfig) {
        initializeRequestExecutors(loadConfig);
        int threadsCount = loadConfig.getThreadsCount();
        runWarmUp(scenario, threadsCount, loadConfig.getWarmUpDuration());
        statisticReporter.startReporting(reportConfig.reportIntervalMillis());
        runLoad(scenario,threadsCount,loadConfig.getTestDuration());
        statisticReporter.stopReporting();
        statisticsService.reset();
        executorService.shutdown();
    }

    private void runWarmUp(Scenario scenario, int threadsCount, Duration warmUpDuration) {
        if (!warmUpDuration.isZero() && !warmUpDuration.isNegative()) {
            runLoad(scenario,threadsCount,warmUpDuration);
            statisticsService.reset();
        }
    }

    private void runLoad(Scenario scenario, int threadsCount, Duration duration) {
        Instant endTime = Instant.now().plus(duration);
        while (Instant.now().isBefore(endTime)) {
            List<Runnable> tasks = new ArrayList<>();
            for (int i = 0; i < threadsCount; i++) {
               tasks.add(()-> scenario.run(requestExecutor));
            }
            tasks.forEach(Runnable::run);
        }
    }
}