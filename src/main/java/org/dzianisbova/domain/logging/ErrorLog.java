package org.dzianisbova.domain.logging;

import org.dzianisbova.domain.api.Request;

public record ErrorLog(Request request, long duration, Throwable e) implements Log {
}
