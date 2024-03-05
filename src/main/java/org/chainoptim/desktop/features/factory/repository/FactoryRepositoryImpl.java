package org.chainoptim.desktop.features.factory.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.repository.FactoryRepository;
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

public class FactoryRepositoryImpl implements FactoryRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public CompletableFuture<Optional<List<Factory>>> getFactoriesByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/factories/organizations/" + organizationId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<Factory>>empty();
                    try {
                        List<Factory> factories = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<Factory>>() {});
                        return Optional.of(factories);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<Factory>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<PaginatedResults<Factory>>> getFactoriesByOrganizationIdAdvanced(
            Integer organizationId,
            String searchQuery,
            String sortOption,
            boolean ascending,
            int page,
            int itemsPerPage
    ) {
        String routeAddress = "http://localhost:8080/api/factories/organizations/advanced" + organizationId.toString()
                + "?searchQuery=" + searchQuery
                + "&sortOption=" + sortOption
                + "&ascending=" + ascending
                + "&page=" + page
                + "&itemsPerPage=" + itemsPerPage;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<PaginatedResults<Factory>>empty();
                    try {
                        PaginatedResults<Factory> factories = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<Factory>>() {});
                        return Optional.of(factories);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<Factory>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Factory>> getFactoryById(Integer factoryId) {
        String routeAddress = "http://localhost:8080/api/factories/" + factoryId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.empty();
                    try {
                        Factory factory = JsonUtil.getObjectMapper().readValue(response.body(), Factory.class);
                        return Optional.of(factory);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return Optional.empty();
                    }
                });
    }
}
