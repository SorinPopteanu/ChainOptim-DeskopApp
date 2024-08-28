package org.chainoptim.desktop.features.demand.clientshipment.dto;

import org.chainoptim.desktop.shared.enums.ShipmentStatus;
import org.chainoptim.desktop.shared.features.location.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateClientShipmentDTO {
    private Integer id;
    private Integer organizationId;
    private Integer clientOrderId;
    private Float quantity;
    private LocalDateTime shipmentStartingDate;
    private LocalDateTime estimatedArrivalDate;
    private LocalDateTime arrivalDate;
    private ShipmentStatus status;
    private Location sourceLocation;
    private Location destinationLocation;
    private Float currentLocationLatitude;
    private Float currentLocationLongitude;
}
