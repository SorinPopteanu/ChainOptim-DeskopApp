package org.chainoptim.desktop.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.text.StringEscapeUtils;

/*
 * Util to configure Json (de)serialization
 *
 */
public class JsonUtil {

    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonUtil() {}

    static {
        // Register the JavaTimeModule
        objectMapper.registerModule(new JavaTimeModule());

        // Ignore unknown properties in JSON input
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Register implicitly
        objectMapper.findAndRegisterModules();
    }

    public static <T> String prepareJsonString(T data) {
        String jsonString = "{}";
        try {
            jsonString = JsonUtil.getObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        String finalJsonString = jsonString;
        return StringEscapeUtils.escapeEcmaScript(finalJsonString);
    }

}
