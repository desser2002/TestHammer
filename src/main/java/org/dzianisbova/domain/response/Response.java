package org.dzianisbova.domain.response;

public class Response {
    private final int statusCode;
    private final String body;
    private final long duration;

    public Response(int statusCode, String body, long duration) {
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

    public long getDuration() {
        return duration;
    }
}
