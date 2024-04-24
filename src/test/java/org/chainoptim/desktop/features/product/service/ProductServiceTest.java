package org.chainoptim.desktop.features.product.service;

import org.chainoptim.desktop.core.user.service.TokenManager;
import org.chainoptim.desktop.features.product.dto.ProductOverviewDTO;
import org.chainoptim.desktop.features.product.dto.ProductsSearchDTO;
import org.chainoptim.desktop.features.product.model.Product;
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
class ProductServiceTest {

    @Mock
    private CachingService<PaginatedResults<Product>> mockCachingService;
    @Mock
    private RequestHandler mockRequestHandler;
    @Mock
    private RequestBuilder mockRequestBuilder;
    @Mock
    private TokenManager mockTokenManager;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getProductsByOrganizationId_ValidResponse() throws Exception {
        // Arrange
        Integer organizationId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/products/organization/" + organizationId.toString()))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();
        CompletableFuture<Result<List<Product>>> expectedFuture = CompletableFuture.completedFuture(new Result<>(new ArrayList<>(), null, 200));

        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<List<Product>>>any())).thenReturn(expectedFuture);
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<List<ProductsSearchDTO>>> resultFuture = productService.getProductsByOrganizationId(organizationId, false);

        // Assert
        verify(mockRequestBuilder).buildReadRequest(contains("organization/" + organizationId), anyString());
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<List<Product>>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }

    @Test
    void getProductsByOrganizationIdAdvanced_CacheHit() throws Exception {
        // Arrange
        Integer organizationId = 1;
        SearchParamsImpl searchParams = new SearchParamsImpl();
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("products", "organization", organizationId.toString(), searchParams);
        PaginatedResults<Product> cachedResults = new PaginatedResults<>(new ArrayList<>(), 0);

        when(mockCachingService.isCached(cacheKey)).thenReturn(true);
        when(mockCachingService.isStale(cacheKey)).thenReturn(false);
        when(mockCachingService.get(cacheKey)).thenReturn(cachedResults);

        // Act
        CompletableFuture<Result<PaginatedResults<Product>>> resultFuture = productService.getProductsByOrganizationIdAdvanced(organizationId, searchParams);

        // Assert
        verify(mockCachingService).get(cacheKey);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
        assertSame(cachedResults, resultFuture.get().getData());
    }

    @Test
    void getProductsByOrganizationIdAdvanced_CacheMiss() throws Exception {
        // Arrange
        Integer organizationId = 1;
        SearchParamsImpl searchParams = new SearchParamsImpl();
        String cacheKey = CacheKeyBuilder.buildAdvancedSearchKey("products", "organization", organizationId.toString(), searchParams);
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/" + cacheKey))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();
        PaginatedResults<Product> fetchedResults = new PaginatedResults<>(new ArrayList<>(), 0);

        when(mockCachingService.isCached(cacheKey)).thenReturn(false);
        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<PaginatedResults<Product>>>any(), any())).thenReturn(CompletableFuture.completedFuture(new Result<>(fetchedResults, null, 200)));
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<PaginatedResults<Product>>> resultFuture = productService.getProductsByOrganizationIdAdvanced(organizationId, searchParams);

        // Assert
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<PaginatedResults<Product>>>any(), any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
        assertSame(fetchedResults, resultFuture.get().getData());
    }

    @Test
    void getProductWithStages_ValidResponse() throws Exception {
        // Arrange
        int productId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/products/" + productId))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();

        CompletableFuture<Result<Product>> expectedFuture = CompletableFuture.completedFuture(new Result<>(new Product(), null, 200));

        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Product>>any())).thenReturn(expectedFuture);
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<Product>> resultFuture = productService.getProductWithStages(productId);

        // Assert
        verify(mockRequestBuilder).buildReadRequest(contains("products/" + productId), anyString());
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Product>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }

    @Test
    void getProductOverview_ValidResponse() throws Exception {
        // Arrange
        int productId = 1;
        String fakeToken = "test-token";
        HttpRequest fakeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/products/" + productId + "/overview"))
                .headers("Authorization", "Bearer " + fakeToken)
                .GET()
                .build();

        CompletableFuture<Result<ProductOverviewDTO>> expectedFuture = CompletableFuture.completedFuture(new Result<>(new ProductOverviewDTO(), null, 200));

        when(mockRequestBuilder.buildReadRequest(anyString(), anyString())).thenReturn(fakeRequest);
        when(mockRequestHandler.sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<ProductOverviewDTO>>any())).thenReturn(expectedFuture);
        when(mockTokenManager.getToken()).thenReturn(fakeToken);

        // Act
        CompletableFuture<Result<ProductOverviewDTO>> resultFuture = productService.getProductOverview(productId);

        // Assert
        verify(mockRequestBuilder).buildReadRequest(contains("products/" + productId), anyString());
        verify(mockRequestHandler).sendRequest(eq(fakeRequest), ArgumentMatchers.<TypeReference<Product>>any());
        assertNotNull(resultFuture);
        assertEquals(HttpURLConnection.HTTP_OK, resultFuture.get().getStatusCode());
    }
}
