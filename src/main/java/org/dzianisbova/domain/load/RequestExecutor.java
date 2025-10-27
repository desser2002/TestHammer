package org.dzianisbova.domain.load;

import org.dzianisbova.domain.api.Request;

public interface RequestExecutor {
    void execute(Request request);
}
