package org.dzianisbova.infrastructure.logging;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.logging.Logger;
import org.dzianisbova.domain.response.Response;

public class InMemoryLogger implements Logger {
    @Override
    public void info(Request request, Response response) {
    }

    @Override
    public void error(Request request, long duration, Throwable exception) {
    }
}
