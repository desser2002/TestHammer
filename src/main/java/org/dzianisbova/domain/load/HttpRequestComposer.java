package org.dzianisbova.domain.load;

import org.dzianisbova.domain.api.HttpMethod;
import org.dzianisbova.domain.api.Request;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class HttpRequestComposer {
    private HttpRequestComposer() {
    }

    public static HttpRequest composeHttpRequest(Request request) {
        HttpRequest.BodyPublisher bodyPublisher = createBodyPublisher(request);

        String fullUrl = composeFullUrl(request);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(fullUrl));
        HttpMethod method = request.getMethod();
        requestBuilder.method(method.name(), bodyPublisher);
        request.getHeaders().forEach(requestBuilder::header);

        return requestBuilder.build();
    }

    private static String composeFullUrl(Request request) {
        String baseUrl = request.getUrl();
        var queryParams = request.getQueryParams();
        if (queryParams.isEmpty()) {
            return baseUrl;
        }
        String queryParamsString = queryParams.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return baseUrl + "?" + queryParamsString;
    }

    private static HttpRequest.BodyPublisher createBodyPublisher(Request request) {
        if (request.getMethod().supportBody() && request.getRequestBody() != null) {
            return HttpRequest.BodyPublishers.ofString(request.getRequestBody());
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }
}
