package org.chainoptim.desktop.features.demand.service;

import org.chainoptim.desktop.core.tenant.user.service.TokenManager;
import org.chainoptim.desktop.features.demand.client.dto.CreateClientDTO;
import org.chainoptim.desktop.features.demand.client.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.demand.client.model.Client;
import org.chainoptim.desktop.features.demand.client.service.ClientWriteServiceImpl;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientWriteServiceTest {

    @Mock
    private RequestHandler requestHandler;
    @Mock
    private RequestBuilder requestBuilder;
    @Mock
    private TokenManager tokenManager;

    @InjectMocks
    private ClientWriteServiceImpl clientWriteService;

    @Test
    void createClient_Successful() throws Exception {
        // Arrange
        CreateClientDTO clientDTO = new CreateClientDTO();
        String clientDTOString = JsonUtil.getObjectMapper().writeValueAsString(clientDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/clients/create"))
                .header("Authorization", "Bearer " + fakeToken)
                .POST(HttpRequest.BodyPublishers.ofString(clientDTOString))
                .build();
        Client expectedClient = new Client();
        CompletableFuture<Result<Client>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedClient, null, HttpURLConnection.HTTP_CREATED));

        when(requestBuilder.buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/clients/create", fakeToken, clientDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Client>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Client>> resultFuture = clientWriteService.createClient(clientDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.POST, "http://localhost:8080/api/v1/clients/create", fakeToken, clientDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Client>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_CREATED, resultFuture.get().getStatusCode());
    }

    @Test
    void updateClient_Successful() throws Exception {
        // Arrange
        UpdateClientDTO clientDTO = new UpdateClientDTO();
        String clientDTOString = JsonUtil.getObjectMapper().writeValueAsString(clientDTO);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/clients/update"))
                .header("Authorization", "Bearer " + fakeToken)
                .PUT(HttpRequest.BodyPublishers.ofString(clientDTOString))
                .build();
        Client expectedClient = new Client();
        CompletableFuture<Result<Client>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedClient, null, HttpURLConnection.HTTP_OK));

        when(requestBuilder.buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/clients/update", fakeToken, clientDTO)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Client>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Client>> resultFuture = clientWriteService.updateClient(clientDTO);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.PUT, "http://localhost:8080/api/v1/clients/update", fakeToken, clientDTO);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Client>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }

    @Test
    void deleteClient_Successful() throws Exception {
        // Arrange
        Integer clientId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/clients/delete/" + clientId))
                .header("Authorization", "Bearer " + fakeToken)
                .DELETE()
                .build();
        Integer expectedResponse = HttpURLConnection.HTTP_OK; // Use an appropriate success code or response
        CompletableFuture<Result<Integer>> expectedFuture = CompletableFuture.completedFuture(new Result<>(expectedResponse, null, HttpURLConnection.HTTP_OK));

        when(requestBuilder.buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/clients/delete/" + clientId, fakeToken, null)).thenReturn(fakeRequest);
        when(requestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any())).thenReturn(expectedFuture);
        when(tokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Integer>> resultFuture = clientWriteService.deleteClient(clientId);

        // Assert
        verify(requestBuilder).buildWriteRequest(HttpMethod.DELETE, "http://localhost:8080/api/v1/clients/delete/" + clientId, fakeToken, null);
        verify(requestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Integer>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }


}
