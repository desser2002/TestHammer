package org.dzianisbova.domain.load;

import org.dzianisbova.domain.api.Request;
import org.dzianisbova.domain.response.Response;

public interface RequestExecutor {
    Response execute (Request request);
}
