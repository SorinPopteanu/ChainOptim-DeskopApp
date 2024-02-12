package org.chainoptim.desktop.shared.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

public class JsonUtil {
    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper();

}
