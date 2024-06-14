package org.chainoptim.desktop.features.supplier.model;

import org.chainoptim.desktop.shared.enums.ShipmentStatus;
import org.chainoptim.desktop.shared.features.location.model.Location;
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
    private LocalDateTime shipmentStartingDate;
    private LocalDateTime estimatedArrivalDate;
    private LocalDateTime arrivalDate;
    private String transporterType;
    private String status;
    private Location sourceLocation;
    private Location destinationLocation;
    private Float currentLocationLatitude;
    private Float currentLocationLongitude;
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
