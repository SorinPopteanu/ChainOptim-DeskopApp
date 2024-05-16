package org.chainoptim.desktop.shared.httphandling;

import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpRequest;

public class RequestBuilderImpl implements RequestBuilder {

    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE_PREFIX = "Bearer ";

    public HttpRequest buildReadRequest(String routeAddress, String jwtToken) {
        return HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .GET()
                .headers(HEADER_KEY, HEADER_VALUE_PREFIX + jwtToken)
                .build();
    }

    public <T> HttpRequest buildWriteRequest(HttpMethod method, String routeAddress, String jwtToken, T requestBody) {
        // Serialize request body
        String requestBodyString;
        try {
            requestBodyString = JsonUtil.getObjectMapper().writeValueAsString(requestBody);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(routeAddress))
                .header(HEADER_KEY, HEADER_VALUE_PREFIX + jwtToken)
                .header("Content-Type", "application/json");

        switch (method) {
            case POST:
                builder.POST(HttpRequest.BodyPublishers.ofString(requestBodyString));
                break;
            case PUT:
                builder.PUT(HttpRequest.BodyPublishers.ofString(requestBodyString));
                break;
            case DELETE:
                builder.method("DELETE", HttpRequest.BodyPublishers.ofString(requestBodyString));
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return builder.build();
    }
}
