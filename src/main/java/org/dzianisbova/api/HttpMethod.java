package org.dzianisbova.api;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE;

    public boolean supportBody() {
        return this == POST || this == PUT || this == PATCH;
    }
}
