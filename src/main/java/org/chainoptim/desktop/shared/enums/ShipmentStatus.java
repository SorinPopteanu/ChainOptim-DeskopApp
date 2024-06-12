package org.chainoptim.desktop.shared.enums;

<<<<<<< HEAD
import org.chainoptim.desktop.shared.util.ShipmentStatusDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ShipmentStatusDeserializer.class)
=======
>>>>>>> 7134550b09d6001d5ce347aaf8ec256d3aec77af
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

<<<<<<< HEAD

=======
>>>>>>> 7134550b09d6001d5ce347aaf8ec256d3aec77af
    public static ShipmentStatus fromString(String statusStr) {
        for (ShipmentStatus status : values()) {
            if (status.name().equalsIgnoreCase(statusStr)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + statusStr + " found");
    }
}
