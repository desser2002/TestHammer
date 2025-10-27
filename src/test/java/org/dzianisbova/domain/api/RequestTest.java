package org.dzianisbova.domain.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {
    @Test
    @DisplayName("Build GET request with URL and method only")
    void testBuildGetRequestMinimal() {
        String url = "http://example.com/api/resource";

        Request request = Request.get(url).build();

        assertAll("Check basic GET request fields",
                () -> assertEquals(url, request.getUrl()),
                () -> assertEquals(HttpMethod.GET, request.getMethod()),
                () -> assertTrue(request.getHeaders().isEmpty()),
                () -> assertTrue(request.getQueryParams().isEmpty()),
                () -> assertNull(request.getRequestBody())
        );
    }

    @Test
    @DisplayName("Build POST request with headers, query params and body")
    void testBuildPostRequestWithHeadersQueryAndBody() {
        String url = "http://example.com/api/resource";
        Map<String, String> headers = Map.of("Content-Type", "application/json", "Accept", "application/json");
        Map<String, String> queryParams = Map.of("page", "1", "limit", "10");
        String body = "{\"key\":\"value\"}";

        Request request = Request.post(url)
                .addHeaders(headers)
                .addQueryParams(queryParams)
                .withBody(body)
                .build();

        assertAll("Check POST request with headers, query params, and body",
                () -> assertEquals(url, request.getUrl()),
                () -> assertEquals(HttpMethod.POST, request.getMethod()),
                () -> assertEquals(headers, request.getHeaders()),
                () -> assertEquals(queryParams, request.getQueryParams()),
                () -> assertEquals(body, request.getRequestBody())
        );
    }

    @Test
    @DisplayName("Build PUT request with null headers and query params")
    void testBuildPutRequestWithNullHeadersQuery() {
        String url = "http://example.com/api/update";
        String body = "update data";

        Request request = Request.put(url)
                .addHeaders(null)
                .withBody(body)
                .addQueryParams(null)
                .build();

        assertAll("Null headers and queryParams should be set as empty",
                () -> assertEquals(url, request.getUrl()),
                () -> assertEquals(HttpMethod.PUT, request.getMethod()),
                () -> assertTrue(request.getHeaders().isEmpty()),
                () -> assertTrue(request.getQueryParams().isEmpty()),
                () -> assertEquals(body, request.getRequestBody())
        );
    }

    @Test
    @DisplayName("Build request should throw if URL or method is null")
    void testBuildThrowsWhenUrlOrMethodNull() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> Request.get(null));
        assertTrue(ex1.getMessage().contains("url and method cannot be null"));
    }

    @Test
    @DisplayName("Build DELETE request with headers and query params")
    void testBuildDeleteRequestWithHeadersAndQueryParams() {
        String url = "http://example.com/api/delete";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("confirm", "true");

        Request request = Request.delete(url)
                .addHeaders(headers)
                .addQueryParams(queryParams)
                .build();

        assertAll("Check DELETE with headers and queryParams, no body",
                () -> assertEquals(url, request.getUrl()),
                () -> assertEquals(HttpMethod.DELETE, request.getMethod()),
                () -> assertEquals(headers, request.getHeaders()),
                () -> assertEquals(queryParams, request.getQueryParams()),
                () -> assertNull(request.getRequestBody())
        );
    }

    @Test
    @DisplayName("Build PATCH request with headers, query params and body")
    void testBuildPatchRequestWithHeadersQueryAndBody() {
        // given
        String url = "http://example.com/api/resource";
        Map<String, String> headers = Map.of("Content-Type", "application/json", "Accept", "application/json");
        Map<String, String> queryParams = Map.of("update", "true");
        String body = "{\"patchKey\":\"patchValue\"}";

        // when
        Request request = Request.patch(url)
                .addHeaders(headers)
                .addQueryParams(queryParams)
                .withBody(body)
                .build();

        // then
        assertAll("Check PATCH request with headers, query params, and body",
                () -> assertEquals(url, request.getUrl()),
                () -> assertEquals(HttpMethod.PATCH, request.getMethod()),
                () -> assertEquals(headers, request.getHeaders()),
                () -> assertEquals(queryParams, request.getQueryParams()),
                () -> assertEquals(body, request.getRequestBody())
        );
    }

}
