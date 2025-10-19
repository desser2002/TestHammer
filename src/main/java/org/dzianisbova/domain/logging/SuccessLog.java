package org.dzianisbova.domain.logging;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.response.Response;

import java.time.Instant;

public record SuccessLog(Request request, Response response, Instant creationTime) implements Log {
}
