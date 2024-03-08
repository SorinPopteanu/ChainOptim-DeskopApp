package org.chainoptim.desktop.features.supplier.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SupplierServiceImpl implements SupplierService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<List<Supplier>>> getSuppliersByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/suppliers/organizations/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<Supplier>>empty();
                    try {
                        List<Supplier> suppliers = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<Supplier>>() {});
                        return Optional.of(suppliers);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<Supplier>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<PaginatedResults<Supplier>>> getSuppliersByOrganizationIdAdvanced(
            Integer organizationId,
            String searchQuery,
            String sortOption,
            boolean ascending,
            int page,
            int itemsPerPage
    ) {
        String routeAddress = "http://localhost:8080/api/suppliers/organizations/advanced/" + organizationId.toString()
                + "?searchQuery=" + searchQuery
                + "&sortOption=" + sortOption
                + "&ascending=" + ascending
                + "&page=" + page
                + "&itemsPerPage=" + itemsPerPage;

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<PaginatedResults<Supplier>>empty();
                    try {
                        PaginatedResults<Supplier> suppliers = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<Supplier>>() {});
                        return Optional.of(suppliers);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<Supplier>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Supplier>> getSupplierById(Integer supplierId) {
        String routeAddress = "http://localhost:8080/api/suppliers/" + supplierId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        Supplier supplier = JsonUtil.getObjectMapper().readValue(response.body(), Supplier.class);
                        return Optional.of(supplier);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return Optional.empty();
                    }
                });
    }
}
