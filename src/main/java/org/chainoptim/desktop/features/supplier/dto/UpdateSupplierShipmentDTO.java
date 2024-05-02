package org.chainoptim.desktop.features.supplier.dto;

import java.time.LocalDateTime;

public class UpdateSupplierShipmentDTO {

    private Integer id;
    private Integer organizationId;
    private Integer supplierOrderId;
    private Float quantity;
    private LocalDateTime shipmentStartingDate;
    private LocalDateTime estimatedArrivalDate;
    private LocalDateTime arrivalDate;
    private String status;
    private Integer sourceLocationId;
    private Integer destinationLocationId;
    private Float currentLocationLatitude;
    private Float currentLocationLongitude;
}
