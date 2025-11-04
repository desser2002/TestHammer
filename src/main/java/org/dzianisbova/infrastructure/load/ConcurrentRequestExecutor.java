package org.dzianisbova.infrastructure.load;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.load.HttpRequestComposer;
import org.dzianisbova.domain.load.RequestExecutor;
import org.dzianisbova.domain.load.ratelimiter.RateLimiter;
import org.dzianisbova.domain.logging.Logger;
import org.dzianisbova.domain.metrics.StatisticsService;
import org.dzianisbova.domain.response.Response;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

public class ConcurrentRequestExecutor implements RequestExecutor {
    private final HttpClient httpClient;
    private final StatisticsService statisticsService;
    private final Logger logger;
    private final RateLimiter rateLimiter;

    public ConcurrentRequestExecutor(Logger logger, HttpClient httpClient,
                                     StatisticsService statisticsService, RateLimiter rateLimiter) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.statisticsService = statisticsService;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void execute(Request request) {
        try {
            rateLimiter.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        HttpRequest httpRequest = HttpRequestComposer.composeHttpRequest(request);
        Instant startTime = Instant.now();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Duration responseTime = Duration.between(startTime, Instant.now());
            Response response = new Response(httpResponse.statusCode(), httpResponse.body(), responseTime);
            statisticsService.recordSuccess(responseTime);
            logger.info(request, response);
        } catch (Exception e) {
            onFail(request, e, startTime);
        }
    }

    private void onFail(Request request, Exception e, Instant startTime) {
        Duration failTime = Duration.between(startTime, Instant.now());
        statisticsService.recordError(failTime);
        logger.error(request, failTime, startTime, e);
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
