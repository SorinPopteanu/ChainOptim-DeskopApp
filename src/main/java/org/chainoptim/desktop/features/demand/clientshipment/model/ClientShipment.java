package org.chainoptim.desktop.features.demand.clientshipment.model;

import org.chainoptim.desktop.shared.enums.ShipmentStatus;
import org.chainoptim.desktop.shared.features.location.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientShipment {

    public ClientShipment(ClientShipment data) {
        this.id = data.id;
        this.createdAt = data.createdAt;
        this.updatedAt = data.updatedAt;
        this.clientOrderId = data.clientOrderId;
        this.quantity = data.quantity;
        this.shipmentStartingDate = data.shipmentStartingDate;
        this.estimatedArrivalDate = data.estimatedArrivalDate;
        this.arrivalDate = data.arrivalDate;
        this.transporterType = data.transporterType;
        this.status = data.status;
        this.sourceLocation = data.sourceLocation;
        this.destinationLocation = data.destinationLocation;
        this.currentLocationLatitude = data.currentLocationLatitude;
        this.currentLocationLongitude = data.currentLocationLongitude;
    }

    private Integer id;
    private Integer clientOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Float quantity;
    private LocalDateTime shipmentStartingDate;
    private LocalDateTime estimatedArrivalDate;
    private LocalDateTime arrivalDate;
    private String transporterType;
    private ShipmentStatus status;
    private Location sourceLocation;
    private Location destinationLocation;
    private Float currentLocationLatitude;
    private Float currentLocationLongitude;
}