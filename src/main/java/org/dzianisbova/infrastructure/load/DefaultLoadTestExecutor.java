package org.dzianisbova.infrastructure.load;

import org.dzianisbova.domain.api.Scenario;
import org.dzianisbova.domain.load.LoadConfig;
import org.dzianisbova.domain.load.LoadTestExecutor;
import org.dzianisbova.domain.load.RequestExecutor;
import org.dzianisbova.domain.load.loadphase.LinearRampUpCalculator;
import org.dzianisbova.domain.load.loadphase.LoadPhase;
import org.dzianisbova.domain.load.loadphase.LoadPhaseCalculator;
import org.dzianisbova.domain.load.ratelimiter.RateLimiter;
import org.dzianisbova.domain.logging.Logger;
import org.dzianisbova.domain.metrics.*;
import org.dzianisbova.infrastructure.load.ratelimiter.TokenBucketRateLimiter;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DefaultLoadTestExecutor implements LoadTestExecutor {
    private final StatisticsService statisticsService;
    private final StatisticReporter statisticReporter;
    private final Logger logger;
    private HttpClient httpClient;
    private final FinalReporter finalReporter;
    private static final long DEFAULT_TOKEN_BUCKET_CAPACITY = 1000;
    private ExecutorService executorService;
    private RequestExecutor requestExecutor;
    private RateLimiter rateLimiter;

    public DefaultLoadTestExecutor(StatisticsService statisticsService, StatisticReporter statisticReporter, Logger logger) {
        this.statisticsService = statisticsService;
        this.statisticReporter = statisticReporter;
        this.finalReporter = new FinalReporter(statisticsService);
        this.logger = logger;
    }

    private void initializeRequestExecutors(LoadConfig loadConfig) {
        httpClient = HttpClient.newHttpClient();
        executorService = Executors.newFixedThreadPool(loadConfig.getThreadsCount());

        rateLimiter = new TokenBucketRateLimiter(loadConfig.getLoadPhase().getStartRps(), DEFAULT_TOKEN_BUCKET_CAPACITY);

        requestExecutor = new ConcurrentRequestExecutor(
                logger,
                httpClient,
                statisticsService, rateLimiter);
    }

    @Override
    public void executeTest(Scenario scenario, LoadConfig loadConfig, ReportConfig reportConfig) {
        initializeRequestExecutors(loadConfig);
        int threadsCount = loadConfig.getThreadsCount();

        LoadPhase loadPhase = loadConfig.getLoadPhase();
        LoadPhaseCalculator calculator = new LinearRampUpCalculator(loadPhase);
        Instant startTime = Instant.now();
        runWarmUp(scenario, threadsCount, loadConfig.getWarmUpDuration());
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Duration elapsed = Duration.between(startTime, Instant.now());
            double currentRps = calculator.getCurrentRps(elapsed);
            rateLimiter.setTokensPerSecond(currentRps);
        }, 0, 500, TimeUnit.MILLISECONDS);
        statisticReporter.startReporting(reportConfig.reportIntervalMillis());
        finalReporter.startLogging(Duration.ofSeconds(2));
        runLoad(scenario, threadsCount, loadConfig.getTestDuration());

        statisticReporter.stopReporting();
        finalReporter.stopLogging();
        try {
            JsonExporter.exportJson("json", finalReporter.getFinalReport());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        statisticsService.reset();
        executorService.shutdownNow();
        httpClient.close();
        scheduler.shutdownNow();
    }

    private void runWarmUp(Scenario scenario, int threadsCount, Duration warmUpDuration) {
        if (!warmUpDuration.isZero() && !warmUpDuration.isNegative()) {
            runLoad(scenario, threadsCount, warmUpDuration);
            finalReporter.reset();
            statisticsService.reset();
        }
    }

    private void runLoad(Scenario scenario, int threadsCount, Duration duration) {
        Instant endTime = Instant.now().plus(duration);
        while (Instant.now().isBefore(endTime)) {
            List<Runnable> tasks = new ArrayList<>();
            for (int i = 0; i < threadsCount; i++) {
                tasks.add(() -> scenario.run(requestExecutor));
            }
            for (Runnable task : tasks) {
                executorService.submit(task);
            }
        }
    }
}