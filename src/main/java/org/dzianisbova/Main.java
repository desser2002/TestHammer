package org.dzianisbova;

import org.dzianisbova.domain.api.LinearScenario;
import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.load.LoadConfig;
import org.dzianisbova.domain.load.LoadTestExecutor;
import org.dzianisbova.domain.load.loadphase.LinearRampUpPhase;
import org.dzianisbova.domain.metrics.ReportConfig;
import org.dzianisbova.domain.metrics.StatisticReporter;
import org.dzianisbova.domain.metrics.StatisticsService;
import org.dzianisbova.infrastructure.load.DefaultLoadTestExecutor;
import org.dzianisbova.infrastructure.logging.InMemoryLogger;
import org.dzianisbova.infrastructure.metrics.ConsoleStatisticsReporter;
import org.dzianisbova.infrastructure.metrics.PerThreadStatisticService;

import java.time.Duration;
import java.util.List;

import static java.time.Duration.ofSeconds;

public class Main {
    public static void main(String[] args) {
        Request getAllDrivers = Request.get("http://localhost:8080/api/drivers").build();

        Request getAllRides = Request.get("http://localhost:8080/api/rides").build();
        LoadConfig loadConfig = new LoadConfig.Builder()
                .threads(20)
                .warmUpDuration(ofSeconds(2))
                .loadPhase(new LinearRampUpPhase(0, 100, ofSeconds(30)))
                .duration(Duration.ofMinutes(2))
                .build();

        StatisticsService statisticsService = new PerThreadStatisticService();
        StatisticReporter reporter = new ConsoleStatisticsReporter(statisticsService);
        ReportConfig reportConfig = new ReportConfig(1000);

        LoadTestExecutor loadTestExecutor = new DefaultLoadTestExecutor(statisticsService, reporter, new InMemoryLogger());

        List<Request> requests = List.of(getAllDrivers, getAllRides);
        loadTestExecutor.executeTest(new LinearScenario(requests), loadConfig, reportConfig);
    }
}