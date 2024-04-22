package org.chainoptim.desktop.shared.httphandling;

import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface RequestHandler {

    <T> CompletableFuture<Result<T>> sendRequest(HttpRequest request, TypeReference<T> typeReference);
    <T> CompletableFuture<Result<T>> sendRequest(HttpRequest request, TypeReference<T> typeReference, Consumer<T> successHandler);
    <T> CompletableFuture<Result<T>> getParsingErrorResult();
}
