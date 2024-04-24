package org.chainoptim.desktop.shared.httphandling;

import java.net.http.HttpRequest;

public interface RequestBuilder {

    HttpRequest buildReadRequest(String routeAddress, String jwtToken);
    <T> HttpRequest buildWriteRequest(HttpMethod method, String routeAddress, String jwtToken, T requestBody);
}
