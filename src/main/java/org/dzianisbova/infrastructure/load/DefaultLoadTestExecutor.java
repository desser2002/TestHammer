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
import java.util.concurrent.*;

public class DefaultLoadTestExecutor implements LoadTestExecutor {
    private final StatisticPublisher statisticPublisher;
    private final StatisticsService statisticsService;
    private final StatisticReporter statisticReporter;
    private final Logger logger;
    private HttpClient httpClient;
    private final FinalReporter finalReporter;
    private static final int DEFAULT_TOKEN_BUCKET_CAPACITY = 20;
    private ThreadPoolExecutor executorService;
    private RequestExecutor requestExecutor;
    private RateLimiter rateLimiter;


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
        statisticPublisher.start(Duration.ofMillis(reportConfig.reportIntervalMillis()));
        statisticReporter.startReporting(reportConfig.reportIntervalMillis());
        finalReporter.start();
        runLoad(scenario, threadsCount, loadConfig.getTestDuration());

        shutdown(scheduler);
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
            try {
                rateLimiter.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            executorService.submit(() -> scenario.run(requestExecutor));
        }
    }

    private void initializeRequestExecutors(LoadConfig loadConfig) {
        httpClient = HttpClient.newHttpClient();

        int threads = loadConfig.getThreadsCount();

        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(threads * 2);

        executorService = new ThreadPoolExecutor(
                threads,
                threads,
                0L,
                TimeUnit.MILLISECONDS,
                workQueue,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

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

        executorService.shutdown();
        awaitTermination(executorService);

        httpClient.close();
    }

    private void awaitTermination(ExecutorService executor) {
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}