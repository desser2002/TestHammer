package org.dzianisbova;

import org.dzianisbova.domain.api.HttpMethod;
import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.load.LoadConfig;
import org.dzianisbova.domain.load.LoadTestExecutor;
import org.dzianisbova.domain.metrics.ReportConfig;
import org.dzianisbova.domain.metrics.StatisticReporter;
import org.dzianisbova.domain.metrics.StatisticsService;
import org.dzianisbova.infrastructure.load.DefaultLoadTestExecutor;
import org.dzianisbova.infrastructure.logging.InMemoryLogger;
import org.dzianisbova.infrastructure.metrics.ConsoleStatisticsReporter;
import org.dzianisbova.infrastructure.metrics.PerThreadStatisticService;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        Request request = new Request.Builder("http://localhost:8080/api/drivers", HttpMethod.GET).build();


        LoadConfig loadConfig = new LoadConfig.Builder()
                .threads(20)
                .duration(Duration.ofMinutes(2))
                .build();

        StatisticsService statisticsService = new PerThreadStatisticService();
        StatisticReporter reporter = new ConsoleStatisticsReporter(statisticsService);
        ReportConfig reportConfig = new ReportConfig(1000);

        LoadTestExecutor loadTestExecutor = new DefaultLoadTestExecutor(statisticsService, reporter, new InMemoryLogger());

        loadTestExecutor.executeTest(request, loadConfig, reportConfig);
    }
}