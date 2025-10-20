package org.dzianisbova.infrastructure.logging;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.logging.ErrorLog;
import org.dzianisbova.domain.logging.Log;
import org.dzianisbova.domain.logging.Logger;
import org.dzianisbova.domain.logging.SuccessLog;
import org.dzianisbova.domain.response.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InMemoryLogger implements Logger {
    private final Queue<Log> logs = new ConcurrentLinkedQueue<>();

    @Override
    public void info(Request request, Response response) {
        Log log = new SuccessLog(request, response, response.getCreationTime());
        logs.add(log);
    }

    @Override
    public void error(Request request, Duration duration, Instant creationTime, Throwable exception) {
        Log log = new ErrorLog(request, duration, creationTime, exception);
        logs.add(log);
    }

    public List<Log> getLogs() {
        return Collections.unmodifiableList(Logger.sortLogs(logs));
    }
}
