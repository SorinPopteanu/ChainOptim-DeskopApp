package org.chainoptim.desktop.features.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Optional.empty;

public class ProductServiceImpl implements ProductService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<List<Product>>> getProductsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/products/organizations/" + organizationId;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<Product>>empty();
                    try {
                        List<Product> products = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<Product>>() {});
                        return Optional.of(products);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<Product>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<PaginatedResults<Product>>> getProductsByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String routeAddress = "http://localhost:8080/api/v1/products/organizations/advanced/" + organizationId.toString()
                + "?searchQuery=" + searchParams.getSearchQuery()
                + "&sortBy=" + searchParams.getSortOption()
                + "&ascending=" + searchParams.getAscending()
                + "&page=" + searchParams.getPage()
                + "&itemsPerPage=" + searchParams.getItemsPerPage();

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<PaginatedResults<Product>>empty();
                    try {
                        PaginatedResults<Product> products = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<Product>>() {});
                        return Optional.of(products);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<Product>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Product>> getProductWithStages(Integer productId) {
        String routeAddress = "http://localhost:8080/api/v1/products/" + productId.toString() + "/stages";

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return new CompletableFuture<>();
        String headerValue = HEADER_VALUE_PREFIX + jwtToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .headers(HEADER_KEY, headerValue)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return empty();
                    try {
                        Product product = JsonUtil.getObjectMapper().readValue(response.body(), Product.class);
                        return Optional.of(product);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return empty();
                    }
                });
    }
}
