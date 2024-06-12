package org.chainoptim.desktop.shared.util;

import org.chainoptim.desktop.shared.enums.ShipmentStatus;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ShipmentStatusDeserializer extends JsonDeserializer<ShipmentStatus> {
    @Override
    public ShipmentStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String statusStr = p.getText();
        return ShipmentStatus.fromString(statusStr);
    }
}