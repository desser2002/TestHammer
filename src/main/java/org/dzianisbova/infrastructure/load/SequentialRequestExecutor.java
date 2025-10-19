package org.dzianisbova.infrastructure.load;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.load.RequestExecutor;
import org.dzianisbova.domain.logging.Logger;
import org.dzianisbova.domain.metrics.StatisticsService;
import org.dzianisbova.domain.response.Response;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

public class SequentialRequestExecutor implements RequestExecutor {
    private final Logger logger;
    private final HttpClient httpClient;
    private final StatisticsService statistic;

    //TODO решить нужен ли и логер и статистик сервис
    public SequentialRequestExecutor(Logger logger, HttpClient httpClient, StatisticsService statistic) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.statistic = statistic;
    }

    @Override
    public Response execute(Request request) {
        String fullUrl = buildFullUrl(request);
        HttpRequest httpRequest = buildHttpRequest(request, fullUrl);
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

    private String buildFullUrl(Request request) {
        StringBuilder url = new StringBuilder(request.getUrl());
        var queryParams = request.getQueryParams();
        if (!queryParams.isEmpty()) {
            url.append("?");
            queryParams.forEach((key, value) -> {
                url.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
                url.append("=");
                url.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                url.append("&");
            });
            url.deleteCharAt(url.length() - 1);
        }
        return url.toString();
    }

    private HttpRequest buildHttpRequest(Request request, String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        HttpRequest.BodyPublisher bodyPublisher = (request.hasBody())
                ? HttpRequest.BodyPublishers.ofString(request.getRequestBody())
                : HttpRequest.BodyPublishers.noBody();
        builder.method(request.getMethod().name(), bodyPublisher);
        request.getHeaders().forEach(builder::header);
        return builder.build();
    }
}
