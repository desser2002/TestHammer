package org.dzianisbova.infrastructure.logging;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.logging.ErrorLog;
import org.dzianisbova.domain.logging.Log;
import org.dzianisbova.domain.logging.Logger;
import org.dzianisbova.domain.logging.SuccessLog;
import org.dzianisbova.domain.response.Response;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryLogger implements Logger {
    private final List<Log> logs = new ArrayList<>();

    @Override
    public void info(Request request, Response response) {
        Log log = new SuccessLog(request, response);
        synchronized (logs) {
            logs.add(log);
        }
    }

    @Override
    public void error(Request request, Duration duration, Throwable exception) {
        Log log = new ErrorLog(request, duration, exception);
        synchronized (logs) {
            logs.add(log);
        }
    }

    public List<Log> getLogs() {
        return Collections.unmodifiableList(logs);
    }
}
