package org.chainoptim.desktop.features.product.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class ProductRepositoryImpl implements ProductRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public Optional<List<Product>> getProductsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/products/organizations/" + organizationId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                List<Product> products = JsonUtil.getObjectMapper().readValue(responseBody, new TypeReference<List<Product>>() {});
                return Optional.of(products);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Product> getProductWithStages(Integer productId) {
        String routeAddress = "http://localhost:8080/api/products/" + productId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                Product product = JsonUtil.getObjectMapper().readValue(responseBody, Product.class);
                return Optional.of(product);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
