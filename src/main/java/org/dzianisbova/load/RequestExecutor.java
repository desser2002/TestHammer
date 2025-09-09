package org.dzianisbova.load;

import org.dzianisbova.api.Request;
import org.dzianisbova.response.Response;

public interface RequestExecutor {
    Response execute (Request request);
}
