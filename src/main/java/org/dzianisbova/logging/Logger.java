package org.dzianisbova.logging;

import org.dzianisbova.api.Request;
import org.dzianisbova.response.Response;

public interface Logger {
    void info(Request request,Response response);

    void error(Request request, long duration, Throwable exception);
}
