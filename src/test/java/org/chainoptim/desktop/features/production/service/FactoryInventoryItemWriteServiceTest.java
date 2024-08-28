package org.chainoptim.desktop.features.production.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.production.dto.CreateFactoryInventoryItemDTO;
import org.chainoptim.desktop.features.production.dto.UpdateFactoryInventoryItemDTO;
import org.chainoptim.desktop.features.production.model.FactoryInventoryItem;
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
class FactoryInventoryItemWriteServiceTest {

    @Mock
    private RequestHandler requestHandler;
    @Mock
    private RequestBuilder requestBuilder;
    @Mock
    private TokenManager tokenManager;

    @InjectMocks
    private FactoryInventoryItemWriteServiceImpl factoryInventoryItemWriteService;

    @Test
    void createFactoryInventoryItem_Successful() throws Exception {
        // Arrange
        CreateFactoryInventoryItemDTO factoryInventoryItemDTO = new CreateFactoryInventoryItemDTO();
        String factoryInventoryItemDTOString = JsonUtil.getObjectMapper().writeValueAsString(factoryInventoryItemDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/factory-inventory-items/create"))
                .header("Authorization", "Bearer " + fakeToken)
                .POST(HttpRequest.BodyPublishers.ofString(factoryInventoryItemDTOString))
                .build();
        FactoryInventoryItem expectedFactoryInventoryItem = new FactoryInventoryItem();
        CompletableFuture<Result<FactoryInventoryItem>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedFactoryInventoryItem, null, HttpURLConnection.HTTP_CREATED));

        when(requestBuilder.buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/factory-inventory-items/create", fakeToken, factoryInventoryItemDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<FactoryInventoryItem>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<FactoryInventoryItem>> resultFuture = factoryInventoryItemWriteService.createFactoryInventoryItem(factoryInventoryItemDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/factory-inventory-items/create", fakeToken, factoryInventoryItemDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<FactoryInventoryItem>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_CREATED, resultFuture.get().getStatusCode());
    }

    @Test
    void updateFactoryInventoryItem_Successful() throws Exception {
        // Arrange
        UpdateFactoryInventoryItemDTO factoryInventoryItemDTO = new UpdateFactoryInventoryItemDTO();
        String factoryInventoryItemDTOString = JsonUtil.getObjectMapper().writeValueAsString(factoryInventoryItemDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/factory-inventory-items/update"))
                .header("Authorization", "Bearer " + fakeToken)
                .PUT(HttpRequest.BodyPublishers.ofString(factoryInventoryItemDTOString))
                .build();
        FactoryInventoryItem expectedFactoryInventoryItem = new FactoryInventoryItem();
        CompletableFuture<Result<FactoryInventoryItem>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedFactoryInventoryItem, null, HttpURLConnection.HTTP_OK));

        when(requestBuilder.buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/factory-inventory-items/update", fakeToken, factoryInventoryItemDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<FactoryInventoryItem>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<FactoryInventoryItem>> resultFuture = factoryInventoryItemWriteService.updateFactoryInventoryItem(factoryInventoryItemDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/factory-inventory-items/update", fakeToken, factoryInventoryItemDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<FactoryInventoryItem>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }

    @Test
    void deleteFactoryInventoryItem_Successful() throws Exception {
        // Arrange
        Integer factoryInventoryItemId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/factory-inventory-items/delete/" + factoryInventoryItemId))
                .header("Authorization", "Bearer " + fakeToken)
                .DELETE()
                .build();
        Integer expectedResponse = HttpURLConnection.HTTP_OK; // Use an appropriate success code or response
        CompletableFuture<Result<Integer>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedResponse, null, HttpURLConnection.HTTP_OK));

        when(requestBuilder.buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/factory-inventory-items/delete/" + factoryInventoryItemId, fakeToken, null)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Integer>> resultFuture = factoryInventoryItemWriteService.deleteFactoryInventoryItem(factoryInventoryItemId);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/factory-inventory-items/delete/" + factoryInventoryItemId, fakeToken, null);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }


}
