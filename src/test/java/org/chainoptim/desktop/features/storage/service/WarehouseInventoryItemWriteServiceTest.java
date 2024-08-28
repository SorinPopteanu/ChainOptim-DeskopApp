package org.chainoptim.desktop.features.storage.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.storage.inventory.dto.CreateWarehouseInventoryItemDTO;
import org.chainoptim.desktop.features.storage.inventory.dto.UpdateWarehouseInventoryItemDTO;
import org.chainoptim.desktop.features.storage.inventory.model.WarehouseInventoryItem;
import org.chainoptim.desktop.features.storage.inventory.service.WarehouseInventoryItemWriteServiceImpl;
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
class WarehouseInventoryItemWriteServiceTest {

    @Mock
    private RequestHandler requestHandler;
    @Mock
    private RequestBuilder requestBuilder;
    @Mock
    private TokenManager tokenManager;

    @InjectMocks
    private WarehouseInventoryItemWriteServiceImpl warehouseInventoryItemWriteService;

    @Test
    void createWarehouseInventoryItem_Successful() throws Exception {
        // Arrange
        CreateWarehouseInventoryItemDTO warehouseInventoryItemDTO = new CreateWarehouseInventoryItemDTO();
        String warehouseInventoryItemDTOString = JsonUtil.getObjectMapper().writeValueAsString(warehouseInventoryItemDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/warehouse-inventory-items/create"))
                .header("Authorization", "Bearer " + fakeToken)
                .POST(HttpRequest.BodyPublishers.ofString(warehouseInventoryItemDTOString))
                .build();
        WarehouseInventoryItem expectedWarehouseInventoryItem = new WarehouseInventoryItem();
        CompletableFuture<Result<WarehouseInventoryItem>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedWarehouseInventoryItem, null, HttpURLConnection.HTTP_CREATED));

        when(requestBuilder.buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/warehouse-inventory-items/create", fakeToken, warehouseInventoryItemDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<WarehouseInventoryItem>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<WarehouseInventoryItem>> resultFuture = warehouseInventoryItemWriteService.createWarehouseInventoryItem(warehouseInventoryItemDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/warehouse-inventory-items/create", fakeToken, warehouseInventoryItemDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<WarehouseInventoryItem>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_CREATED, resultFuture.get().getStatusCode());
    }

    @Test
    void updateWarehouseInventoryItem_Successful() throws Exception {
        // Arrange
        UpdateWarehouseInventoryItemDTO warehouseInventoryItemDTO = new UpdateWarehouseInventoryItemDTO();
        String warehouseInventoryItemDTOString = JsonUtil.getObjectMapper().writeValueAsString(warehouseInventoryItemDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/warehouse-inventory-items/update"))
                .header("Authorization", "Bearer " + fakeToken)
                .PUT(HttpRequest.BodyPublishers.ofString(warehouseInventoryItemDTOString))
                .build();
        WarehouseInventoryItem expectedWarehouseInventoryItem = new WarehouseInventoryItem();
        CompletableFuture<Result<WarehouseInventoryItem>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedWarehouseInventoryItem, null, HttpURLConnection.HTTP_OK));

        when(requestBuilder.buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/warehouse-inventory-items/update", fakeToken, warehouseInventoryItemDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<WarehouseInventoryItem>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<WarehouseInventoryItem>> resultFuture = warehouseInventoryItemWriteService.updateWarehouseInventoryItem(warehouseInventoryItemDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/warehouse-inventory-items/update", fakeToken, warehouseInventoryItemDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<WarehouseInventoryItem>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }

    @Test
    void deleteWarehouseInventoryItem_Successful() throws Exception {
        // Arrange
        Integer warehouseInventoryItemId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/warehouse-inventory-items/delete/" + warehouseInventoryItemId))
                .header("Authorization", "Bearer " + fakeToken)
                .DELETE()
                .build();
        Integer expectedResponse = HttpURLConnection.HTTP_OK; // Use an appropriate success code or response
        CompletableFuture<Result<Integer>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedResponse, null, HttpURLConnection.HTTP_OK));

        when(requestBuilder.buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/warehouse-inventory-items/delete/" + warehouseInventoryItemId, fakeToken, null)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Integer>> resultFuture = warehouseInventoryItemWriteService.deleteWarehouseInventoryItem(warehouseInventoryItemId);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/warehouse-inventory-items/delete/" + warehouseInventoryItemId, fakeToken, null);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }
}
