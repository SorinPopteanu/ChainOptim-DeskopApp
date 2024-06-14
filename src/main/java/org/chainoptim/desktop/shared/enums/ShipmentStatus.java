package org.chainoptim.desktop.shared.enums;

import org.chainoptim.desktop.shared.util.ShipmentStatusDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ShipmentStatusDeserializer.class)
public enum ShipmentStatus {
    INITIATED,
    NEGOTIATED,
    PLACED,
    DELIVERED,
    CANCELED;

    @Override
    public String toString() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }

    public static ShipmentStatus fromString(String statusStr) {
        for (ShipmentStatus status : values()) {
            if (status.name().equalsIgnoreCase(statusStr)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + statusStr + " found");
    }
}
