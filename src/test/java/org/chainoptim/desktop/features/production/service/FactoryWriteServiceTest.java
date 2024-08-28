package org.chainoptim.desktop.features.production.service;

import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.features.production.factory.dto.CreateFactoryDTO;
import org.chainoptim.desktop.features.production.factory.dto.UpdateFactoryDTO;
import org.chainoptim.desktop.features.production.factory.model.Factory;
import org.chainoptim.desktop.features.production.factory.service.FactoryWriteServiceImpl;
import org.chainoptim.desktop.shared.httphandling.HttpMethod;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FactoryWriteServiceTest {

    @Mock
    private RequestHandler requestHandler;
    @Mock
    private RequestBuilder requestBuilder;
    @Mock
    private TokenManager tokenManager;

    @InjectMocks
    private FactoryWriteServiceImpl factoryWriteService;

    @Test
    void createFactory_Successful() throws Exception {
        // Arrange
        CreateFactoryDTO factoryDTO = new CreateFactoryDTO();
        String factoryDTOString = JsonUtil.getObjectMapper().writeValueAsString(factoryDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/factories/create"))
                .header("Authorization", "Bearer " + fakeToken)
                .POST(HttpRequest.BodyPublishers.ofString(factoryDTOString))
                .build();
        Factory expectedFactory = new Factory();
        CompletableFuture<Result<Factory>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedFactory, null, HttpURLConnection.HTTP_CREATED));

        when(requestBuilder.buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/factories/create", fakeToken, factoryDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Factory>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Factory>> resultFuture = factoryWriteService.createFactory(factoryDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/factories/create", fakeToken, factoryDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Factory>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_CREATED, resultFuture.get().getStatusCode());
    }

    @Test
    void updateFactory_Successful() throws Exception {
        // Arrange
        UpdateFactoryDTO factoryDTO = new UpdateFactoryDTO();
        String factoryDTOString = JsonUtil.getObjectMapper().writeValueAsString(factoryDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/factories/update"))
                .header("Authorization", "Bearer " + fakeToken)
                .PUT(HttpRequest.BodyPublishers.ofString(factoryDTOString))
                .build();
        Factory expectedFactory = new Factory();
        CompletableFuture<Result<Factory>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedFactory, null, HttpURLConnection.HTTP_OK));

        when(requestBuilder.buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/factories/update", fakeToken, factoryDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Factory>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Factory>> resultFuture = factoryWriteService.updateFactory(factoryDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/factories/update", fakeToken, factoryDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Factory>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }

    @Test
    void deleteFactory_Successful() throws Exception {
        // Arrange
        Integer factoryId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/factories/delete/" + factoryId))
                .header("Authorization", "Bearer " + fakeToken)
                .DELETE()
                .build();
        Integer expectedResponse = HttpURLConnection.HTTP_OK; // Use an appropriate success code or response
        CompletableFuture<Result<Integer>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedResponse, null, HttpURLConnection.HTTP_OK));

        when(requestBuilder.buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/factories/delete/" + factoryId, fakeToken, null)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Integer>> resultFuture = factoryWriteService.deleteFactory(factoryId);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/factories/delete/" + factoryId, fakeToken, null);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }


}
