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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class ConcurrentRequestExecutor implements RequestExecutor {
    private final HttpClient httpClient;
    private final StatisticsService statisticsService;
    private final Logger logger;
    private final ExecutorService executor;

    public ConcurrentRequestExecutor(Logger logger, HttpClient httpClient,
                                     StatisticsService statisticsService, ExecutorService executor) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.statisticsService = statisticsService;
        this.executor = executor;
    }

    public List<Response> executeAll(List<Request> requests) {
        List<CompletableFuture<Response>> responses = requests.stream()
                .map(request -> CompletableFuture.supplyAsync(() -> execute(request), executor))
                .toList();

        return responses.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    @Override
    public Response execute(Request request) {
        HttpRequest httpRequest = HttpRequestComposer.composeHttpRequest(request);
        Instant startTime = Instant.now();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Duration responseTime = Duration.between(startTime, Instant.now());
            Response response = new Response(httpResponse.statusCode(), httpResponse.body(), responseTime);
            statisticsService.recordSuccess(responseTime);
            logger.info(request, response);
            return response;
        } catch (Exception e) {
            return onFail(request, e, startTime);
        }
    }

    private Response onFail(Request request, Exception e, Instant startTime) {
        Duration failTime = Duration.between(startTime, Instant.now());
        statisticsService.recordError(failTime);
        logger.error(request, failTime, startTime, e);
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        return new Response(Response.ERROR_STATUS, e.getMessage(), failTime);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
