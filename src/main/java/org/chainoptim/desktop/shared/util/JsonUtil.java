package org.chainoptim.desktop.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

/**
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
        return escapeEcmaScript(jsonString);
    }

    public static String escapeEcmaScript(String input) {
        StringBuilder builder = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"': builder.append("\\\""); break;
                case '\\': builder.append("\\\\"); break;
                case '\b': builder.append("\\b"); break;
                case '\f': builder.append("\\f"); break;
                case '\n': builder.append("\\n"); break;
                case '\r': builder.append("\\r"); break;
                case '\t': builder.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        builder.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        builder.append(c);
                    }
            }
        }
        return builder.toString();
    }

}
