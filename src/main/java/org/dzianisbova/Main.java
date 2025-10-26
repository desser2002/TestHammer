package org.dzianisbova;

import org.dzianisbova.domain.api.HttpMethod;
import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.load.RequestExecutor;
import org.dzianisbova.domain.metrics.StatisticReporter;
import org.dzianisbova.domain.metrics.StatisticsService;
import org.dzianisbova.infrastructure.load.ConcurrentRequestExecutor;
import org.dzianisbova.infrastructure.logging.InMemoryLogger;
import org.dzianisbova.infrastructure.metrics.ConsoleStatisticsReporter;
import org.dzianisbova.infrastructure.metrics.PerThreadStatisticService;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        Request request = new Request.Builder("http://localhost:8080/api/drivers", HttpMethod.GET).build();
        HttpClient httpClient = HttpClient.newHttpClient();
        StatisticsService statisticsService = new PerThreadStatisticService();
        int threadsCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);
        RequestExecutor executor = new ConcurrentRequestExecutor(
                new InMemoryLogger(), httpClient, statisticsService, executorService);
        StatisticReporter reporter = new ConsoleStatisticsReporter(statisticsService);
        reporter.startReporting(1000);
        int totalRequests = 50000;
        int batchSize = 100;
        for (int i = 0; i < totalRequests; i += batchSize) {
            List<Request> batch = new ArrayList<>();
            for (int j = 0; j < batchSize && (i + j) < totalRequests; j++) {
                batch.add(request);
            }

            executor.executeAll(batch);
        }

        reporter.stopReporting();
        statisticsService.reset();
        executorService.shutdown();
    }
}