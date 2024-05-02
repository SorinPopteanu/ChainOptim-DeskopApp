package org.chainoptim.desktop.features.supplier.model;

import org.chainoptim.desktop.shared.features.location.model.Location;
import java.time.LocalDateTime;

public class SupplierShipment {

    public SupplierShipment(SupplierShipment data) {
        this.id = data.id;
        this.supplierOrderId = data.supplierOrderId;
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
    private Integer supplierOrderId;
    private Float quantity;
    private LocalDateTime shipmentStartingDate;
    private LocalDateTime estimatedArrivalDate;
    private LocalDateTime arrivalDate;
    private String transporterType;
    private String status;
    private Location sourceLocation;
    private Location destinationLocation;
    private Float currentLocationLatitude;
    private Float currentLocationLongitude;
}
