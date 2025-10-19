package org.dzianisbova.domain.api;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String url;
    private final HttpMethod httpMethod;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final String requestBody;

    public Request(Builder builder) {
        this.url = builder.url;
        this.httpMethod = builder.method;
        this.headers = builder.headers;
        this.queryParams = builder.queryParams;
        this.requestBody = builder.requestBody;
    }

    public String getUrl() {
        return url;
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

    public String getRequestBody() {
        return requestBody;
    }

    public interface RequestBuilder {
        RequestBuilder addHeaders(Map<String, String> headers);

        RequestBuilder addQueryParams(Map<String, String> queryParams);

        Request build();
    }

    public static class Builder implements RequestBuilder {
        private final String url;
        private final HttpMethod method;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> queryParams = new HashMap<>();
        protected String requestBody;

        public Builder(String url, HttpMethod method) {
            this.url = url;
            this.method = method;
        }

        public Builder addHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder addQueryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public Request build() {
            if (url != null && method != null) {
                return new Request(this);
            } else {
                throw new IllegalArgumentException(this.getClass().getName() + " has null url or httpMethod");
            }
        }
    }

    public static class RequestBuilderWithBody extends Builder {
        protected RequestBuilderWithBody(String url, HttpMethod httpMethod) {
            super(url, httpMethod);
        }

        public RequestBuilderWithBody withBody(String body) {
            this.requestBody = body;
            return this;
        }
    }

    public static RequestBuilder newBuilder(String url, HttpMethod method) {
        if (method.supportBody()) {
            return new RequestBuilderWithBody(url, method);
        } else {
            return new Builder(url, method);
        }
    }
}
