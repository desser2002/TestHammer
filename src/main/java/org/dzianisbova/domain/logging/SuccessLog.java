package org.dzianisbova.domain.logging;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.response.Response;

public record SuccessLog(Request request, Response response) implements Log {
}
