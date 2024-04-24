package org.chainoptim.desktop.core.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private HttpClient mockClient;
    @Mock
    private TokenManager tokenManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private void setupMockResponse(int statusCode, String responseBody) throws IOException, InterruptedException {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(statusCode);
        if (responseBody != null) {
            when(mockResponse.body()).thenReturn(responseBody);
        }
        when(mockClient.send(any(HttpRequest.class), any())).thenAnswer((Answer<HttpResponse<String>>) invocation -> mockResponse);
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Arrange
        setupMockResponse(HttpURLConnection.HTTP_OK, "{\"accessToken\":\"dummyToken\"}");

        // Act
        boolean result = authenticationService.login("user", "pass");

        // Assert
        verify(mockClient).send(any(HttpRequest.class), any());
        assertTrue(result);
    }

    @Test
    void testLoginFailure() throws Exception {
        // Arrange
        setupMockResponse(401, null);

        // Act
        boolean result = authenticationService.login("user", "pass");

        // Assert
        verify(mockClient).send(any(HttpRequest.class), any());
        assertFalse(result);
    }

    @Test
    void testValidateJWTTokenSuccess() throws Exception {
        // Arrange
        setupMockResponse(200, null);

        // Act
        boolean result = authenticationService.validateJWTToken("validToken");

        // Assert
        verify(mockClient).send(any(HttpRequest.class), any());
        assertTrue(result);
    }

    @Test
    void testValidateJWTTokenFailure() throws Exception {
        // Arrange
        setupMockResponse(401, null);

        // Act
        boolean result = authenticationService.validateJWTToken("invalidToken");

        // Assert
        verify(mockClient).send(any(HttpRequest.class), any());
        Assertions.assertFalse(result);
    }

    @Test
    void testGetUsernameFromJWTTokenSuccess() throws Exception {
        // Arrange
        setupMockResponse(200, "username");

        // Act
        Optional<String> result = authenticationService.getUsernameFromJWTToken("validToken");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("username", result.get());
        verify(mockClient).send(any(HttpRequest.class), any());
    }

    @Test
    void testGetUsernameFromJWTTokenFailure() throws Exception {
        // Arrange
        setupMockResponse(401, null);

        // Act
        Optional<String> result = authenticationService.getUsernameFromJWTToken("invalidToken");

        // Assert
        Assertions.assertFalse(result.isPresent());
        verify(mockClient).send(any(HttpRequest.class), any());
    }

}
