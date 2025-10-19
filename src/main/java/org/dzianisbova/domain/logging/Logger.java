package org.dzianisbova.domain.logging;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.response.Response;

import java.time.Duration;

public interface Logger {
    void info(Request request,Response response);

    void error(Request request, Duration duration, Throwable exception);
}
