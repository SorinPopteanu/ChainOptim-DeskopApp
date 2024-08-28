package org.chainoptim.desktop.features.demand.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.demand.clientorder.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.demand.clientorder.model.ClientOrder;
import org.chainoptim.desktop.features.demand.clientorder.service.ClientOrdersWriteServiceImpl;
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
class ClientOrderWriteServiceTest {

    @Mock
    private RequestHandler requestHandler;
    @Mock
    private RequestBuilder requestBuilder;
    @Mock
    private TokenManager tokenManager;

    @InjectMocks
    private ClientOrdersWriteServiceImpl clientOrderWriteService;

    @Test
    void createClientOrder_Successful() throws Exception {
        // Arrange
        CreateClientOrderDTO clientOrderDTO = new CreateClientOrderDTO();
        String clientOrderDTOString = JsonUtil.getObjectMapper().writeValueAsString(clientOrderDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/client-orders/create"))
                .header("Authorization", "Bearer " + fakeToken)
                .POST(HttpRequest.BodyPublishers.ofString(clientOrderDTOString))
                .build();
        ClientOrder expectedClientOrder = new ClientOrder();
        CompletableFuture<Result<ClientOrder>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedClientOrder, null, HttpURLConnection.HTTP_CREATED));

        when(requestBuilder.buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/client-orders/create", fakeToken, clientOrderDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<ClientOrder>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<ClientOrder>> resultFuture = clientOrderWriteService.createClientOrder(clientOrderDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/client-orders/create", fakeToken, clientOrderDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<ClientOrder>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_CREATED, resultFuture.get().getStatusCode());
    }
//
//    @Test
//    void updateClientOrder_Successful() throws Exception {
//        // Arrange
//        UpdateClientOrderDTO clientOrderDTO = new UpdateClientOrderDTO();
//        String clientOrderDTOString = JsonUtil.getObjectMapper().writeValueAsString(clientOrderDTO);
//        String fakeToken = "test-token";
//        HttpRequest fakeRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/api/v1/client-orders/update"))
//                .header("Authorization", "Bearer " + fakeToken)
//                .PUT(HttpRequest.BodyPublishers.ofString(clientOrderDTOString))
//                .build();
//        ClientOrder expectedClientOrder = new ClientOrder();
//        CompletableFuture<Result<ClientOrder>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedClientOrder, null, HttpURLConnection.HTTP_OK));
//
//        when(requestBuilder.buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/client-orders/update", fakeToken, clientOrderDTO)).thenReturn(fakeRequest);
//        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<ClientOrder>>any())).thenReturn(expectedFuture);
//        when(tokenManager.getToken()).thenReturn(fakeToken);
//
//        // Act
//        CompletableFuture<Result<ClientOrder>> resultFuture = clientOrderWriteService.updateClientOrder(clientOrderDTO);
//
//        // Assert
//        verify(requestBuilder).buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/client-orders/update", fakeToken, clientOrderDTO);
//        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<ClientOrder>>any());
//        assertNotNull(resultFuture);
//        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
//    }
//
//    @Test
//    void deleteClientOrder_Successful() throws Exception {
//        // Arrange
//        Integer clientOrderId = 1;
//        String fakeToken = "test-token";
//        HttpRequest fakeRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/api/v1/client-orders/delete/" + clientOrderId))
//                .header("Authorization", "Bearer " + fakeToken)
//                .DELETE()
//                .build();
//        Integer expectedResponse = HttpURLConnection.HTTP_OK; // Use an appropriate success code or response
//        CompletableFuture<Result<Integer>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedResponse, null, HttpURLConnection.HTTP_OK));
//
//        when(requestBuilder.buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/client-orders/delete/" + clientOrderId, fakeToken, null)).thenReturn(fakeRequest);
//        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any())).thenReturn(expectedFuture);
//        when(tokenManager.getToken()).thenReturn(fakeToken);
//
//        // Act
//        CompletableFuture<Result<Integer>> resultFuture = clientOrderWriteService.deleteClientOrder(clientOrderId);
//
//        // Assert
//        verify(requestBuilder).buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/client-orders/delete/" + clientOrderId, fakeToken, null);
//        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any());
//        assertNotNull(resultFuture);
//        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
//    }


}
