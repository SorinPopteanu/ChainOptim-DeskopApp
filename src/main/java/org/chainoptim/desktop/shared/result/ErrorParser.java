package org.chainoptim.desktop.shared.result;

import org.chainoptim.desktop.shared.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.http.HttpResponse;

public class ErrorParser {

    private ErrorParser() {}

    public static <T> Result<T> parseError(HttpResponse<String> httpResponse) {
        Result<T> result = new Result<>();
        Error error;

        try {
            error = JsonUtil.getObjectMapper().readValue(httpResponse.body(), Error.class);
        } catch (JsonProcessingException e) {
            error = new Error();
            error.setMessage("An unknown error occurred.");
        }

        result.setError(error);
        result.setStatusCode(httpResponse.statusCode());

        return result;
    }
}
