package org.chainoptim.desktop.shared.result;

import org.chainoptim.desktop.shared.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RequestHandlerImpl implements RequestHandler {

    private final HttpClient client = HttpClient.newHttpClient();

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
                        Error error = new Error(new Date(), "An unknown error occurred.", "");
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
                        T data = JsonUtil.getObjectMapper().readValue(response.body(), typeReference);

                        successHandler.accept(data);

                        return new Result<>(data, null, response.statusCode());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Error error = new Error(new Date(), "An unknown error occurred.", "");
                        return new Result<>(null, error, response.statusCode());
                    }
                });
    }
}
