package org.dzianisbova.domain.logging;

import org.dzianisbova.domain.api.Request;

import java.time.Duration;
import java.time.Instant;

public record ErrorLog(Request request, Duration duration, Instant creationTime, Throwable e) implements Log {
}
