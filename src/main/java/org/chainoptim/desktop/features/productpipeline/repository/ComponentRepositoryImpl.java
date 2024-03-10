package org.chainoptim.desktop.features.productpipeline.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.features.productpipeline.model.Component;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class ComponentRepositoryImpl implements ComponentRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public Optional<List<Component>> getComponentsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/v1/components/organizations/" + organizationId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                List<Component> components = JsonUtil.getObjectMapper().readValue(responseBody, new TypeReference<List<Component>>() {});
                return Optional.of(components);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
