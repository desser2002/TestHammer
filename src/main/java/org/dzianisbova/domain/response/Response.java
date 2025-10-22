package org.dzianisbova.domain.response;

import java.time.Duration;
import java.time.Instant;

public class Response {
    public static final int ERROR_STATUS = -1;
    private final int statusCode;
    private final String body;
    private final Duration duration;
    private final Instant creationTime;

    public Response(int statusCode, String body, Duration duration) {
        this.statusCode = statusCode;
        this.body = body;
        this.duration = duration;
        this.creationTime = Instant.now();
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

    public Instant getCreationTime() {
        return creationTime;
    }
}
