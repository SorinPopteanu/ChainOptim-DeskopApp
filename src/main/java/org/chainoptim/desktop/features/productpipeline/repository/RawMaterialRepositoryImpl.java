package org.chainoptim.desktop.features.productpipeline.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chainoptim.desktop.features.productpipeline.model.RawMaterial;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class RawMaterialRepositoryImpl implements RawMaterialRepository {

    private final HttpClient client = HttpClient.newHttpClient();

    public Optional<List<RawMaterial>> getRawMaterialsByOrganizationId(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/raw-materials/organizations/" + organizationId.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                List<RawMaterial> rawMaterials = JsonUtil.getObjectMapper().readValue(responseBody, new TypeReference<List<RawMaterial>>() {});
                return Optional.of(rawMaterials);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
