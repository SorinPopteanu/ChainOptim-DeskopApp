package org.chainoptim.desktop.features.supplier.model;

import org.chainoptim.desktop.shared.enums.ShipmentStatus;
import org.chainoptim.desktop.shared.features.location.model.Location;
<<<<<<< HEAD
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupplierShipment {

    private Integer id;
    private Integer supplierOrderId;
    private String companyId;
    private Integer componentId;
    private String componentName;
    private Float quantity;
    private Float deliveredQuantity;
=======
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
>>>>>>> 7134550b09d6001d5ce347aaf8ec256d3aec77af
    private LocalDateTime shipmentStartingDate;
    private LocalDateTime estimatedArrivalDate;
    private LocalDateTime arrivalDate;
    private String transporterType;
    private ShipmentStatus status;
    private Location sourceLocation;
    private Location destinationLocation;
    private Float currentLocationLatitude;
    private Float currentLocationLongitude;
<<<<<<< HEAD
    private Integer organizationId;
    private Integer supplierId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SupplierShipment deepCopy() {
        return SupplierShipment.builder()
//                .id(this.id)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .supplierOrderId(this.supplierOrderId)
                .quantity(this.quantity)
                .shipmentStartingDate(this.shipmentStartingDate)
                .estimatedArrivalDate(this.estimatedArrivalDate)
                .arrivalDate(this.arrivalDate)
                .transporterType(this.transporterType)
                .status(this.status)
                .sourceLocation(this.sourceLocation)
                .destinationLocation(this.destinationLocation)
                .currentLocationLatitude(this.currentLocationLatitude)
                .currentLocationLongitude(this.currentLocationLongitude)
                .organizationId(this.organizationId)
                .supplierId(this.supplierId)
                .build();
    }
}
=======
}
>>>>>>> 7134550b09d6001d5ce347aaf8ec256d3aec77af
