package org.dzianisbova.domain.api;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String baseUrl;
    private final HttpMethod httpMethod;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final String body;
    private final boolean hasBody;

    public Request(Builder builder) {
        this.baseUrl = builder.url;
        this.httpMethod = builder.httpMethod;
        this.headers = builder.headers;
        this.queryParams = builder.queryParams;
        this.body = builder.body;
        this.hasBody = body != null && !body.isEmpty() && !body.isBlank() && httpMethod.supportBody();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public boolean hasBody() {
        return hasBody;
    }

    public static class Builder {
        private final String url;
        private final HttpMethod httpMethod;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> queryParams = new HashMap<>();
        private String body;

        public Builder(String url, HttpMethod httpMethod) {
            this.url = url;
            this.httpMethod = httpMethod;
        }

        public Builder withHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder withQueryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }

        public Request build() {
            if (url != null && httpMethod != null) {
                return new Request(this);
            } else {
                throw new IllegalArgumentException(this.getClass().getName() + " has null url or httpMethod");
            }
        }
    }
}
