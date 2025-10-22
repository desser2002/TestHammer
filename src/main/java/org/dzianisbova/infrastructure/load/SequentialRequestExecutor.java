package org.dzianisbova.infrastructure.load;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.load.HttpRequestComposer;
import org.dzianisbova.domain.load.RequestExecutor;
import org.dzianisbova.domain.logging.Logger;
import org.dzianisbova.domain.metrics.StatisticsService;
import org.dzianisbova.domain.response.Response;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class SequentialRequestExecutor implements RequestExecutor {
    private final Logger logger;
    private final HttpClient httpClient;
    private final StatisticsService statistic;

    public SequentialRequestExecutor(Logger logger, HttpClient httpClient, StatisticsService statistic) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.statistic = statistic;
    }

    @Override
    public Response execute(Request request) {
        HttpRequest httpRequest = HttpRequestComposer.composeHttpRequest(request);
        Instant startTime = Instant.now();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Duration duration = Duration.between(startTime, Instant.now());
            Response response = new Response(httpResponse.statusCode(), httpResponse.body(), duration);
            logger.info(request, response);
            statistic.recordSuccess(duration);

            return response;
        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());

            logger.error(request, duration, Instant.now(), e);
            statistic.recordError(duration);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            return new Response(-1, e.getMessage(), duration);
        }
    }

    @Override
    public List<Response> executeAll(List<Request> requests) {
        return requests.stream()
                .map(this::execute)
                .toList();
    }
}
