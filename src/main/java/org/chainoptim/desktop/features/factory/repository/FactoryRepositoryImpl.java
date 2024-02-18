package org.chainoptim.desktop.features.factory.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class FactoryRepositoryImpl implements FactoryRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public Optional<List<Factory>> getFactoriesByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/factories/organizations/" + organizationId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                List<Factory> factories = JsonUtil.getObjectMapper().readValue(responseBody, new TypeReference<List<Factory>>() {});
                return Optional.of(factories);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Factory> getFactoryById(Integer factoryId) {
        String routeAddress = "http://localhost:8080/api/factories/" + factoryId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                Factory factory = JsonUtil.getObjectMapper().readValue(responseBody, Factory.class);
                return Optional.of(factory);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
