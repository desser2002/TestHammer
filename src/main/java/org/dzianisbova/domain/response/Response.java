package org.dzianisbova.domain.response;

import java.time.Duration;

public class Response {
    private final int statusCode;
    private final String body;
    private final Duration duration;

    public Response(int statusCode, String body, Duration duration) {
        this.statusCode = statusCode;
        this.body = body;
        this.duration = duration;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Duration getDuration() {
        return duration;
    }
}
