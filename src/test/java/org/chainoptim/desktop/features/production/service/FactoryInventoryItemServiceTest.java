package org.chainoptim.desktop.features.production.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.production.model.FactoryInventoryItem;
import org.chainoptim.desktop.shared.caching.CacheKeyBuilder;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.enums.SearchMode;
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
class FactoryInventoryItemServiceTest {

    @Mock
    private CachingService<PaginatedResults<FactoryInventoryItem>> mockCachingService;
    @Mock
    private RequestHandler mockRequestHandler;
    @Mock
    private RequestBuilder mockRequestBuilder;
    @Mock
    private TokenManager mockTokenManager;

    @InjectMocks
    private FactoryInventoryItemServiceImpl factoryInventoryItemService;

    @Test
    void getFactoryInventoryItemsByOrganizationId_ValidResponse() throws Exception {
        // Arrange
        Integer organizationId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/factory-inventory-items/organization/" + organizationId))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();
        CompletableFuture<Result<List<FactoryInventoryItem>>> expectedFuture = CompletableFuture.completedFuture(new Result<>(new ArrayList<>(), null, 200));

        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<List<FactoryInventoryItem>>>any())).thenReturn(expectedFuture);
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<List<FactoryInventoryItem>>> resultFuture = factoryInventoryItemService.getFactoryInventoryItemsByOrganizationId(organizationId);

        // Assert
        verify(mockRequestBuilder).buildReadRequest(contains("organization/" + organizationId), anyString());
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<List<FactoryInventoryItem>>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }

    @Test
    void getFactoryInventoryItemsByOrganizationIdAdvanced_CacheHit() throws Exception {
        // Arrange
        Integer clientId = 1;
        SearchParamsImpl searchParams = new SearchParamsImpl();
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("factory-inventory-items", "factory", clientId.toString(), searchParams);
        PaginatedResults<FactoryInventoryItem> cachedResults = new PaginatedResults<>(new ArrayList<>(), 0);

        when(mockCachingService.isCached(cacheKey)).thenReturn(true);
        when(mockCachingService.isStale(cacheKey)).thenReturn(false);
        when(mockCachingService.get(cacheKey)).thenReturn(cachedResults);

        // Act
        CompletableFuture<Result<PaginatedResults<FactoryInventoryItem>>> resultFuture = factoryInventoryItemService.getFactoryInventoryItemsByFactoryIdAdvanced(clientId, searchParams, SearchMode.SECONDARY);

        // Assert
        verify(mockCachingService).get(cacheKey);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
        assertSame(cachedResults, resultFuture.get().getData());
    }

    @Test
    void getFactoryInventoryItemsByOrganizationIdAdvanced_CacheMiss() throws Exception {
        // Arrange
        Integer clientId = 1;
        SearchParamsImpl searchParams = new SearchParamsImpl();
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("factory-inventory-items", "factory", clientId.toString(), searchParams);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/" + cacheKey))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();
        PaginatedResults<FactoryInventoryItem> fetchedResults = new PaginatedResults<>(new ArrayList<>(), 0);

        when(mockCachingService.isCached(cacheKey)).thenReturn(false);
        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<PaginatedResults<FactoryInventoryItem>>>any(), any())).thenReturn(CompletableFuture.completedFuture(new Result<>(fetchedResults, null, 200)));
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<PaginatedResults<FactoryInventoryItem>>> resultFuture = factoryInventoryItemService.getFactoryInventoryItemsByFactoryIdAdvanced(clientId, searchParams, SearchMode.SECONDARY);

        // Assert
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<PaginatedResults<FactoryInventoryItem>>>any(), any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
        assertSame(fetchedResults, resultFuture.get().getData());
    }

    @Test
    void getFactoryInventoryItemById_ValidResponse() throws Exception {
        // Arrange
        int factoryInventoryItemId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/factory-inventory-items/" + factoryInventoryItemId))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();

        CompletableFuture<Result<FactoryInventoryItem>> expectedFuture = CompletableFuture.completedFuture(new Result<>(new FactoryInventoryItem(), null, 200));

        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<FactoryInventoryItem>>any())).thenReturn(expectedFuture);
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<FactoryInventoryItem>> resultFuture = factoryInventoryItemService.getFactoryInventoryItemById(factoryInventoryItemId);

        // Assert
        verify(mockRequestBuilder).buildReadRequest(contains("factory-inventory-items/" + factoryInventoryItemId), anyString());
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<FactoryInventoryItem>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());

    }
}
