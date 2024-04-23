package org.chainoptim.desktop.shared.http;

import org.chainoptim.desktop.shared.httphandling.RequestHandlerImpl;
import org.chainoptim.desktop.shared.httphandling.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RequestHandlerTest {

    @Mock
    private HttpClient mockHttpClient;
    @Mock
    private HttpResponse<String> mockHttpResponse;

    private RequestHandlerImpl requestHandler;

    @BeforeEach
    void setUp() {
        requestHandler = new RequestHandlerImpl(mockHttpClient);
    }

    @Test
    void testSendRequest_Success() throws Exception {
        // Arrange
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://example.com/api"))
                .GET()
                .build();
        TypeReference<String> typeReference = new TypeReference<>() {};
        String json = "\"response data\"";
        CompletableFuture<HttpResponse<String>> future = CompletableFuture.completedFuture(mockHttpResponse);

        Mockito.when(mockHttpClient.sendAsync(Mockito.any(HttpRequest.class), Mockito.any(HttpResponse.BodyHandler.class)))
                .thenReturn(future);
        Mockito.when(mockHttpResponse.statusCode()).thenReturn(200);
        Mockito.when(mockHttpResponse.body()).thenReturn(json);

        // Act
        CompletableFuture<Result<String>> resultFuture = requestHandler.sendRequest(request, typeReference);

        // Assert
        Result<String> result = resultFuture.join();
        assertEquals("response data", result.getData());
        assertNull(result.getError());
        assertEquals(HttpURLConnection.HTTP_OK, result.getStatusCode());
    }

    @Test
    void testSendRequest_ServerError() throws Exception {
        // Arrange
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://example.com/api"))
                .GET()
                .build();
        CompletableFuture<HttpResponse<String>> future = CompletableFuture.completedFuture(mockHttpResponse);

        Mockito.when(mockHttpClient.sendAsync(Mockito.any(HttpRequest.class), Mockito.any(HttpResponse.BodyHandler.class)))
                .thenReturn(future);
        Mockito.when(mockHttpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_INTERNAL_ERROR);
        Mockito.when(mockHttpResponse.body()).thenReturn("Internal Server Error");

        // Act
        CompletableFuture<Result<String>> resultFuture = requestHandler.sendRequest(request, new TypeReference<>() {});

        // Assert
        Result<String> result = resultFuture.join();
        assertNotNull(result.getError());
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatusCode());
    }
}
