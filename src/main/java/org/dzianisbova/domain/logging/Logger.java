package org.dzianisbova.domain.logging;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.response.Response;

public interface Logger {
    void info(Request request,Response response);

    void error(Request request, long duration, Throwable exception);
}
