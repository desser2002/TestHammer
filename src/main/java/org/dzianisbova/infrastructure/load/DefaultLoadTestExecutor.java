package org.dzianisbova.infrastructure.load;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.load.LoadConfig;
import org.dzianisbova.domain.load.LoadTestExecutor;
import org.dzianisbova.domain.load.RequestExecutor;
import org.dzianisbova.domain.logging.Logger;
import org.dzianisbova.domain.metrics.ReportConfig;
import org.dzianisbova.domain.metrics.StatisticReporter;
import org.dzianisbova.domain.metrics.StatisticsService;

import java.net.http.HttpClient;
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
                statisticsService,
                executorService);
    }

    @Override
    public void executeTest(Request request, LoadConfig loadConfig, ReportConfig reportConfig) {
        initializeRequestExecutors(loadConfig);
        statisticReporter.startReporting(reportConfig.reportIntervalMillis());

        Instant endTime = Instant.now().plus(loadConfig.getDuration());
        int threads = loadConfig.getThreadsCount();
        while (Instant.now().isBefore(endTime)) {
            List<Request> batch = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                batch.add(request);
            }
            requestExecutor.executeAll(batch);
        }
        statisticReporter.stopReporting();
        statisticsService.reset();
        executorService.shutdown();
    }
}