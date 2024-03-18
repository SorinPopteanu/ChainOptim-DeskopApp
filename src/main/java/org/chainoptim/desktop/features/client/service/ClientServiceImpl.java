package org.chainoptim.desktop.features.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.features.client.model.Client;
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

public class ClientServiceImpl implements ClientService {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public CompletableFuture<Optional<List<Client>>> getClientsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/clients/organizations/" + organizationId.toString();

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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<List<Client>>empty();
                    try {
                        List<Client> clients = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<List<Client>>() {});
                        return Optional.of(clients);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<List<Client>>empty();
                    }
                });
    }


    public CompletableFuture<Optional<PaginatedResults<Client>>> getClientsByOrganizationIdAdvanced(
            Integer organizationId,
            SearchParams searchParams
    ) {
        String routeAddress = "http://localhost:8080/api/v1/clients/organizations/advanced/" + organizationId.toString()
                + "?searchQuery=" + searchParams.getSearchQuery()
                + "&sortOption=" + searchParams.getSortOption()
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
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) return Optional.<PaginatedResults<Client>>empty();
                    try {
                        PaginatedResults<Client> clients = JsonUtil.getObjectMapper().readValue(response.body(), new TypeReference<PaginatedResults<Client>>() {});
                        return Optional.of(clients);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<PaginatedResults<Client>>empty();
                    }
                });
    }

    public CompletableFuture<Optional<Client>> getClientById(Integer clientId) {
        String routeAddress = "http://localhost:8080/api/v1/clients/" + clientId.toString();

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
                        Client client = JsonUtil.getObjectMapper().readValue(response.body(), Client.class);
                        return Optional.of(client);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return Optional.empty();
                    }
                });
    }
}
