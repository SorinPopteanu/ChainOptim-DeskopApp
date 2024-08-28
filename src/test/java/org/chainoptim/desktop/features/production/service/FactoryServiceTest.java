package org.chainoptim.desktop.features.production.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.production.model.Factory;
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
class FactoryServiceTest {

    @Mock
    private CachingService<PaginatedResults<Factory>> mockCachingService;
    @Mock
    private RequestHandler mockRequestHandler;
    @Mock
    private RequestBuilder mockRequestBuilder;
    @Mock
    private TokenManager mockTokenManager;

    @InjectMocks
    private FactoryServiceImpl factoryService;

    @Test
    void getFactoriesByOrganizationId_ValidResponse() throws Exception {
        // Arrange
        Integer organizationId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/factories/organization/" + organizationId.toString()))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();
        CompletableFuture<Result<List<Factory>>> expectedFuture = CompletableFuture.completedFuture(new Result<>(new ArrayList<>(), null, 200));

        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<List<Factory>>>any())).thenReturn(expectedFuture);
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<List<Factory>>> resultFuture = factoryService.getFactoriesByOrganizationId(organizationId);

        // Assert
        verify(mockRequestBuilder).buildReadRequest(contains("organization/" + organizationId), anyString());
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<List<Factory>>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }

    @Test
    void getFactoriesByOrganizationIdAdvanced_CacheHit() throws Exception {
        // Arrange
        Integer organizationId = 1;
        SearchParamsImpl searchParams = new SearchParamsImpl();
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("factories", "organization", organizationId.toString(), searchParams);
        PaginatedResults<Factory> cachedResults = new PaginatedResults<>(new ArrayList<>(), 0);

        when(mockCachingService.isCached(cacheKey)).thenReturn(true);
        when(mockCachingService.isStale(cacheKey)).thenReturn(false);
        when(mockCachingService.get(cacheKey)).thenReturn(cachedResults);

        // Act
        CompletableFuture<Result<PaginatedResults<Factory>>> resultFuture = factoryService.getFactoriesByOrganizationIdAdvanced(organizationId, searchParams);

        // Assert
        verify(mockCachingService).get(cacheKey);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
        assertSame(cachedResults, resultFuture.get().getData());
    }

    @Test
    void getFactoriesByOrganizationIdAdvanced_CacheMiss() throws Exception {
        // Arrange
        Integer organizationId = 1;
        SearchParamsImpl searchParams = new SearchParamsImpl();
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("factories", "organization", organizationId.toString(), searchParams);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/" + cacheKey))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();
        PaginatedResults<Factory> fetchedResults = new PaginatedResults<>(new ArrayList<>(), 0);

        when(mockCachingService.isCached(cacheKey)).thenReturn(false);
        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<PaginatedResults<Factory>>>any(), any())).thenReturn(CompletableFuture.completedFuture(new Result<>(fetchedResults, null, 200)));
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<PaginatedResults<Factory>>> resultFuture = factoryService.getFactoriesByOrganizationIdAdvanced(organizationId, searchParams);

        // Assert
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<PaginatedResults<Factory>>>any(), any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
        assertSame(fetchedResults, resultFuture.get().getData());
    }

    @Test
    void getFactoryById_ValidResponse() throws Exception {
        // Arrange
        int factoryId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/factories/" + factoryId))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();

        CompletableFuture<Result<Factory>> expectedFuture = CompletableFuture.completedFuture(new Result<>(new Factory(), null, 200));

        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Factory>>any())).thenReturn(expectedFuture);
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Factory>> resultFuture = factoryService.getFactoryById(factoryId);

        // Assert
        verify(mockRequestBuilder).buildReadRequest(contains("factories/" + factoryId), anyString());
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Factory>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());

    }
}
