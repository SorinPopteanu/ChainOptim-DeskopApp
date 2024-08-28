package org.chainoptim.desktop.features.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.chainoptim.desktop.shared.features.location.model.Location;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSupplierShipmentDTO {

    private Integer organizationId;
    private Integer supplierId;
    private Integer supplierOrderId;
    private String companyId;
    private Integer componentId;
    private String componentName;
    private Float quantity;
    private Float deliveredQuantity;
    private LocalDateTime shipmentStartingDate;
    private LocalDateTime estimatedArrivalDate;
    private LocalDateTime arrivalDate;
    private String status;
    private Integer sourceLocationId;
    private Integer destinationLocationId;
    private Location sourceLocation;
    private Location destinationLocation;
    private Float currentLocationLatitude;
    private Float currentLocationLongitude;
}
