package org.dzianisbova.domain.api;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String url;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final String requestBody;

    private Request(BaseBuilder<?> builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers != null ? builder.headers : new HashMap<>();
        this.queryParams = builder.queryParams != null ? builder.queryParams : new HashMap<>();
        if (builder instanceof BuilderWithBody builderWithBody) {
            this.requestBody = builderWithBody.body;
        } else {
            this.requestBody = null;
        }
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRequestBody() {
        return requestBody;
    }

    private abstract static class BaseBuilder<T extends BaseBuilder<T>> {
        protected final String url;
        protected final HttpMethod method;
        protected Map<String, String> headers = new HashMap<>();
        protected Map<String, String> queryParams = new HashMap<>();

        protected BaseBuilder(String url, HttpMethod method) {
            if (url == null || method == null) {
                throw new IllegalArgumentException("url and method cannot be null");
            }
            this.url = url;
            this.method = method;
        }

        protected abstract T self();

        public T addHeaders(Map<String, String> headers) {
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return self();
        }


        public T addQueryParams(Map<String, String> queryParams) {
            if (queryParams != null) {
                this.queryParams.putAll(queryParams);
            }
            return self();
        }

        public abstract Request build();
    }

    public static class BuilderWithoutBody extends BaseBuilder<BuilderWithoutBody> {
        public BuilderWithoutBody(String url, HttpMethod method) {
            super(url, method);
            if (method.supportBody()) {
                throw new IllegalArgumentException(method + " supports body. Use BodyBuilder instead.");
            }
        }

        @Override
        protected BuilderWithoutBody self() {
            return this;
        }

        @Override
        public Request build() {
            return new Request(this);
        }
    }

    public static class BuilderWithBody extends BaseBuilder<BuilderWithBody> {
        private String body;

        public BuilderWithBody(String url, HttpMethod method) {
            super(url, method);
            if (!method.supportBody()) {
                throw new IllegalArgumentException(method + " does not support body. Use SimpleBuilder instead.");
            }
        }

        @Override
        protected BuilderWithBody self() {
            return this;
        }

        public BuilderWithBody withBody(String body) {
            if (body == null) {
                throw new IllegalArgumentException("Body cannot be null");
            }
            this.body = body;
            return this;
        }

        @Override
        public Request build() {
            if (body == null) {
                throw new IllegalStateException("Body must be provided for HTTP method " + method);
            }
            return new Request(this);
        }
    }

    public static BuilderWithoutBody get(String url) {
        return new BuilderWithoutBody(url, HttpMethod.GET);
    }

    public static BuilderWithoutBody delete(String url) {
        return new BuilderWithoutBody(url, HttpMethod.DELETE);
    }

    public static BuilderWithBody post(String url) {
        return new BuilderWithBody(url, HttpMethod.POST);
    }

    public static BuilderWithBody put(String url) {
        return new BuilderWithBody(url, HttpMethod.PUT);
    }

    public static BuilderWithBody patch(String url) {
        return new BuilderWithBody(url, HttpMethod.PATCH);
    }
}
