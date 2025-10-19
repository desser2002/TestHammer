package org.dzianisbova.infrastructure.load;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.load.RequestExecutor;
import org.dzianisbova.domain.response.Response;

public class ConcurrentRequestExecutor implements RequestExecutor {
    @Override
    public Response execute(Request request) {
        return null;
    }
}
