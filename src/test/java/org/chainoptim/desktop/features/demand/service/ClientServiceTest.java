package org.chainoptim.desktop.features.demand.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.demand.client.model.Client;
import org.chainoptim.desktop.features.demand.client.service.ClientServiceImpl;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParamsImpl;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private CachingService<PaginatedResults<Client>> mockCachingService;
    @Mock
    private RequestHandler mockRequestHandler;
    @Mock
    private RequestBuilder mockRequestBuilder;
    @Mock
    private TokenManager mockTokenManager;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    void getClientsByOrganizationId_ValidResponse() throws Exception {
        // Arrange
        Integer organizationId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/clients/organization/" + organizationId.toString()))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();
        CompletableFuture<Result<List<Client>>> expectedFuture = CompletableFuture.completedFuture(new Result<>(new ArrayList<>(), null, 200));

        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<List<Client>>>any())).thenReturn(expectedFuture);
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<List<Client>>> resultFuture = clientService.getClientsByOrganizationId(organizationId);

        // Assert
        verify(mockRequestBuilder).buildReadRequest(contains("organization/" + organizationId), anyString());
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<List<Client>>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }

    @Test
    void getClientsByOrganizationIdAdvanced_CacheHit() throws Exception {
        // Arrange
        Integer organizationId = 1;
        SearchParamsImpl searchParams = new SearchParamsImpl();
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("clients", "organization", organizationId.toString(), searchParams);
        PaginatedResults<Client> cachedResults = new PaginatedResults<>(new ArrayList<>(), 0);

        when(mockCachingService.isCached(cacheKey)).thenReturn(true);
        when(mockCachingService.isStale(cacheKey)).thenReturn(false);
        when(mockCachingService.get(cacheKey)).thenReturn(cachedResults);

        // Act
        CompletableFuture<Result<PaginatedResults<Client>>> resultFuture = clientService.getClientsByOrganizationIdAdvanced(organizationId, searchParams);

        // Assert
        verify(mockCachingService).get(cacheKey);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
        assertSame(cachedResults, resultFuture.get().getData());
    }

    @Test
    void getClientsByOrganizationIdAdvanced_CacheMiss() throws Exception {
        // Arrange
        Integer organizationId = 1;
        SearchParamsImpl searchParams = new SearchParamsImpl();
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("clients", "organization", organizationId.toString(), searchParams);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/" + cacheKey))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();
        PaginatedResults<Client> fetchedResults = new PaginatedResults<>(new ArrayList<>(), 0);

        when(mockCachingService.isCached(cacheKey)).thenReturn(false);
        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<PaginatedResults<Client>>>any(), any())).thenReturn(CompletableFuture.completedFuture(new Result<>(fetchedResults, null, 200)));
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<PaginatedResults<Client>>> resultFuture = clientService.getClientsByOrganizationIdAdvanced(organizationId, searchParams);

        // Assert
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<PaginatedResults<Client>>>any(), any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
        assertSame(fetchedResults, resultFuture.get().getData());
    }

    @Test
    void getClientById_ValidResponse() throws Exception {
        // Arrange
        int clientId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/clients/" + clientId))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();

        CompletableFuture<Result<Client>> expectedFuture = CompletableFuture.completedFuture(new Result<>(new Client(), null, 200));

        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Client>>any())).thenReturn(expectedFuture);
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Client>> resultFuture = clientService.getClientById(clientId);

        // Assert
        verify(mockRequestBuilder).buildReadRequest(contains("clients/" + clientId), anyString());
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Client>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());

    }
}
