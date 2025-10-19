package org.dzianisbova.domain.logging;

import org.dzianisbova.domain.api.Request;

import java.time.Duration;

public record ErrorLog(Request request, Duration duration, Throwable e) implements Log {
}
