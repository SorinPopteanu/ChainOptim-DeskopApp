package org.chainoptim.desktop.shared.http;

import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilderImpl;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RequestBuilderTest {

    private RequestBuilderImpl requestBuilder;

    @BeforeEach
    void setUp() {
        requestBuilder = new RequestBuilderImpl();
    }

    @Test
    void testBuildReadRequest() {
        // Arrange
        String route = "http://example.com/api";
        String token = "token123";

        // Act
        HttpRequest request = requestBuilder.buildReadRequest(route, token);

        // Assert
        assertEquals("GET", request.method());
        assertTrue(request.headers().map().containsKey("Authorization"));
        assertEquals("Bearer token123", request.headers().map().get("Authorization").getFirst());
        assertEquals(URI.create(route), request.uri());
    }

    @Test
    void testBuildWriteRequest() {
        // Arrange
        HttpMethod method = HttpMethod.POST;
        String route = "http://example.com/api";
        String token = "token123";

        Result<String> requestBody = new Result<>("requestBody", null, HttpURLConnection.HTTP_OK);

        // Act
        HttpRequest request = requestBuilder.buildWriteRequest(method, route, token, requestBody);

        // Assert
        assertEquals("POST", request.method());
        assertTrue(request.headers().map().containsKey("Authorization"));
        assertEquals("Bearer token123", request.headers().map().get("Authorization").getFirst());
        assertTrue(request.headers().map().containsKey("Content-Type"));
        assertEquals("application/json", request.headers().map().get("Content-Type").getFirst());
        assertEquals(URI.create(route), request.uri());
    }
}
