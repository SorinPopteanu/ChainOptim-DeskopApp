package org.chainoptim.desktop.core.organization.repository;

import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class OrganizationRepositoryImpl implements OrganizationRepository {

    public Optional<Organization> getOrganizationById(Integer organizationId) {
        String routeAddress = "http://localhost:8080/api/organizations/" + organizationId.toString();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();
                Organization organization = JsonUtil.getObjectMapper().readValue(responseBody, Organization.class);
                return Optional.of(organization);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
