package org.dzianisbova.domain.logging;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.response.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public interface Logger {
    void info(Request request, Response response);

    void error(Request request, Duration duration, Instant creationTime, Throwable exception);

    static List<Log> sortLogs(Collection<Log> logsToSort) {
        var list = new ArrayList<>(logsToSort);
        list.sort(Comparator.comparing(Log::creationTime));
        return list;
    }
}
