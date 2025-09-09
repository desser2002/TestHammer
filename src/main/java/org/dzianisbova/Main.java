package org.dzianisbova;

import org.dzianisbova.domain.api.HttpMethod;
import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.load.RequestExecutor;
import org.dzianisbova.domain.metrics.StatisticReporter;
import org.dzianisbova.domain.metrics.StatisticsService;
import org.dzianisbova.infrastructure.load.SequentialRequestExecutor;
import org.dzianisbova.infrastructure.logging.InMemoryLogger;
import org.dzianisbova.infrastructure.metrics.ConsoleStatisticsReporter;
import org.dzianisbova.infrastructure.metrics.InMemoryStatisticService;

import java.net.http.HttpClient;

public class Main {
    public static void main(String[] args) {
        Request request = new Request.Builder("http://localhost:8080/api/drivers", HttpMethod.GET).build();
        HttpClient httpClient = HttpClient.newHttpClient();
        StatisticsService statisticsService = new InMemoryStatisticService();
        RequestExecutor executor = new SequentialRequestExecutor(new InMemoryLogger(), httpClient, statisticsService);
        StatisticReporter reporter = new ConsoleStatisticsReporter(statisticsService);
        reporter.startReporting(5000);
        for (int i = 0; i < 50000; i++) {
            executor.execute(request);
        }
        reporter.stopReporting();
    }
}