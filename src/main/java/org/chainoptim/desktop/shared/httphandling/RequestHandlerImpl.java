package org.chainoptim.desktop.shared.httphandling;

import org.chainoptim.desktop.shared.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;

import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RequestHandlerImpl implements RequestHandler {

    private final HttpClient client;

    @Inject
    public RequestHandlerImpl(HttpClient client) {
        this.client = client;
    }

    public <T> CompletableFuture<Result<T>> sendRequest(HttpRequest request, TypeReference<T> typeReference) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                        return ErrorParser.parseError(response);
                    }
                    try {
                        T data = JsonUtil.getObjectMapper().readValue(response.body(), typeReference);
                        return new Result<>(data, null, response.statusCode());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Error error = new Error(new Date(), "An error occurred while deserializing the received data.", "");
                        return new Result<>(null, error, response.statusCode());
                    }
                });
    }

    public <T> CompletableFuture<Result<T>> sendRequest(HttpRequest request, TypeReference<T> typeReference, Consumer<T> successHandler) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                        return ErrorParser.parseError(response);
                    }
                    try {
                        String responseBody = response.body();
                        if (responseBody == null || responseBody.isEmpty()) {
                            return new Result<>(null, null, response.statusCode()); // Return default value
                        }
                        T data = JsonUtil.getObjectMapper().readValue(responseBody, typeReference);

                        successHandler.accept(data);

                        return new Result<>(data, null, response.statusCode());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Error error = new Error(new Date(), "An error occurred while deserializing the received data.", "");
                        return new Result<>(null, error, response.statusCode());
                    }
                });
    }

    public <T> CompletableFuture<Result<T>> getParsingErrorResult() {
        return CompletableFuture.completedFuture(
                new Result<>(null, new Error(new Date(), "An error occurred while processing the data.", ""), 0));
    }
}
