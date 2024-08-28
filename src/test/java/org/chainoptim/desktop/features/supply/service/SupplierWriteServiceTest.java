package org.chainoptim.desktop.features.supply.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.supply.dto.CreateSupplierDTO;
import org.chainoptim.desktop.features.supply.dto.UpdateSupplierDTO;
import org.chainoptim.desktop.features.supply.model.Supplier;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierWriteServiceTest {

    @Mock
    private RequestHandler requestHandler;
    @Mock
    private RequestBuilder requestBuilder;
    @Mock
    private TokenManager tokenManager;

    @InjectMocks
    private SupplierWriteServiceImpl supplierWriteService;

    @Test
    void createSupplier_Successful() throws Exception {
        // Arrange
        CreateSupplierDTO supplierDTO = new CreateSupplierDTO();
        String supplierDTOString = JsonUtil.getObjectMapper().writeValueAsString(supplierDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/suppliers/create"))
                .header("Authorization", "Bearer " + fakeToken)
                .POST(HttpRequest.BodyPublishers.ofString(supplierDTOString))
                .build();
        Supplier expectedSupplier = new Supplier();
        CompletableFuture<Result<Supplier>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedSupplier, null, HttpURLConnection.HTTP_CREATED));

        when(requestBuilder.buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/suppliers/create", fakeToken, supplierDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Supplier>>any(), any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Supplier>> resultFuture = supplierWriteService.createSupplier(supplierDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/suppliers/create", fakeToken, supplierDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Supplier>>any(), any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_CREATED, resultFuture.get().getStatusCode());
    }

    @Test
    void updateSupplier_Successful() throws Exception {
        // Arrange
        UpdateSupplierDTO supplierDTO = new UpdateSupplierDTO();
        String supplierDTOString = JsonUtil.getObjectMapper().writeValueAsString(supplierDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/suppliers/update"))
                .header("Authorization", "Bearer " + fakeToken)
                .PUT(HttpRequest.BodyPublishers.ofString(supplierDTOString))
                .build();
        Supplier expectedSupplier = new Supplier();
        CompletableFuture<Result<Supplier>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedSupplier, null, HttpURLConnection.HTTP_OK));

        when(requestBuilder.buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/suppliers/update", fakeToken, supplierDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Supplier>>any(), any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Supplier>> resultFuture = supplierWriteService.updateSupplier(supplierDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/suppliers/update", fakeToken, supplierDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Supplier>>any(), any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }

    @Test
    void deleteSupplier_Successful() throws Exception {
        // Arrange
        Integer supplierId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/suppliers/delete/" + supplierId))
                .header("Authorization", "Bearer " + fakeToken)
                .DELETE()
                .build();
        Integer expectedResponse = HttpURLConnection.HTTP_OK; // Use an appropriate success code or response
        CompletableFuture<Result<Integer>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedResponse, null, HttpURLConnection.HTTP_OK));

        when(requestBuilder.buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/suppliers/delete/" + supplierId, fakeToken, null)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any(), any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Integer>> resultFuture = supplierWriteService.deleteSupplier(supplierId);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/suppliers/delete/" + supplierId, fakeToken, null);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any(), any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }


}
