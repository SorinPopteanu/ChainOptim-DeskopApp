package org.chainoptim.desktop.shared.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/*
 * Util to configure Json (de)serialization
 *
 */
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Register the JavaTimeModule
        objectMapper.registerModule(new JavaTimeModule());

        // Ignore unknown properties in JSON input
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Register implicitly
        objectMapper.findAndRegisterModules();
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
