package org.dzianisbova.infrastructure.load;

import org.dzianisbova.domain.api.Scenario;
import org.dzianisbova.domain.load.LoadConfig;
import org.dzianisbova.domain.load.LoadTestExecutor;
import org.dzianisbova.domain.load.RequestExecutor;
import org.dzianisbova.domain.load.ScenarioTask;
import org.dzianisbova.domain.load.loadphase.LinearRampUpCalculator;
import org.dzianisbova.domain.load.loadphase.LoadPhase;
import org.dzianisbova.domain.load.loadphase.LoadPhaseCalculator;
import org.dzianisbova.domain.load.ratelimiter.RateLimiter;
import org.dzianisbova.domain.logging.Logger;
import org.dzianisbova.domain.metrics.*;
import org.dzianisbova.infrastructure.load.ratelimiter.TokenBucketRateLimiter;
import org.dzianisbova.infrastructure.load.requestorchestrator.RateLimitedTaskDispatcher;
import org.dzianisbova.infrastructure.load.taskproducer.QueueFeeder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

public class DefaultLoadTestExecutor implements LoadTestExecutor {
    private final StatisticPublisher statisticPublisher;
    private final StatisticsService statisticsService;
    private final StatisticReporter statisticReporter;
    private final Logger logger;
    private HttpClient httpClient;
    private final FinalReporter finalReporter;
    private static final int DEFAULT_TOKEN_BUCKET_CAPACITY = 20;
    private ExecutorService executorService;
    private RequestExecutor requestExecutor;
    private RateLimiter rateLimiter;
    private BlockingQueue<Runnable> taskQueue;


    public DefaultLoadTestExecutor(StatisticPublisher statisticPublisher, StatisticsService statisticsService, StatisticReporter statisticReporter, Logger logger) {
        this.statisticPublisher = statisticPublisher;
        this.statisticsService = statisticsService;
        this.statisticReporter = statisticReporter;
        this.finalReporter = new FinalReporter(statisticPublisher);
        this.logger = logger;
    }

    @Override
    public void executeTest(Scenario scenario, LoadConfig loadConfig, ReportConfig reportConfig) {
        initializeRequestExecutors(loadConfig);

        LoadPhase loadPhase = loadConfig.getLoadPhase();
        LoadPhaseCalculator calculator = new LinearRampUpCalculator(loadPhase);
        Instant startTime = Instant.now();

        runWarmUp(scenario, loadConfig.getWarmUpDuration());

        ScheduledExecutorService rampUpScheduler = Executors.newSingleThreadScheduledExecutor();

        rampUpScheduler.scheduleAtFixedRate(() -> {
            Duration elapsed = Duration.between(startTime, Instant.now());
            double currentRps = calculator.getCurrentRps(elapsed);
            rateLimiter.setTokensPerSecond(currentRps);
        }, 0, 500, TimeUnit.MILLISECONDS);

        statisticPublisher.start(Duration.ofMillis(reportConfig.reportIntervalMillis()));
        statisticReporter.startReporting(reportConfig.reportIntervalMillis());
        finalReporter.start();
        runLoad(scenario, loadConfig.getTestDuration());

        shutdown(rampUpScheduler);
    }

    private void runWarmUp(Scenario scenario, Duration warmUpDuration) {
        if (!warmUpDuration.isZero() && !warmUpDuration.isNegative()) {
            runLoad(scenario, warmUpDuration);
            finalReporter.reset();
            statisticsService.reset();
        }
    }

    private void runLoad(Scenario scenario, Duration duration) {

        ScenarioTask scenarioTask = new ScenarioTask(scenario, requestExecutor);
        QueueFeeder queueFeeder = new QueueFeeder(taskQueue, scenarioTask, duration);

        RateLimitedTaskDispatcher dispatcher = new RateLimitedTaskDispatcher(taskQueue, executorService, rateLimiter);

        queueFeeder.start();
        dispatcher.start();

        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void initializeRequestExecutors(LoadConfig loadConfig) {
        httpClient = HttpClient.newHttpClient();

        int threads = loadConfig.getThreadsCount();
        int queueCapacity = threads * 10;

        taskQueue = new ArrayBlockingQueue<>(queueCapacity);

        executorService = Executors.newFixedThreadPool(threads);

        rateLimiter = new TokenBucketRateLimiter(
                loadConfig.getLoadPhase().getStartRps(),
                DEFAULT_TOKEN_BUCKET_CAPACITY
        );
        requestExecutor = new ConcurrentRequestExecutor(
                logger,
                httpClient,
                statisticsService
        );
    }

    private void shutdown(ScheduledExecutorService scheduler) {
        scheduler.shutdownNow();
        awaitTermination(scheduler);

        statisticPublisher.stop();
        statisticReporter.stopReporting();
        finalReporter.stop();

        try {
            JsonExporter.exportJson("json", finalReporter.getFinalReport());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        statisticsService.reset();

        executorService.shutdownNow();
        awaitTermination(executorService);

        httpClient.close();
    }

    private void awaitTermination(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}