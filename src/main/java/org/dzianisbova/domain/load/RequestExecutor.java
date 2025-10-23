package org.dzianisbova.domain.load;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.response.Response;

import java.util.List;

public interface RequestExecutor {
    Response execute(Request request);

    List<Response> executeAll(List<Request> request);
}
