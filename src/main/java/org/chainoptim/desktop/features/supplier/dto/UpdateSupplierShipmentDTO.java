package org.chainoptim.desktop.features.supplier.dto;

<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

=======
import org.chainoptim.desktop.shared.enums.ShipmentStatus;
import org.chainoptim.desktop.shared.features.location.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> 7134550b09d6001d5ce347aaf8ec256d3aec77af
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSupplierShipmentDTO {
<<<<<<< HEAD

=======
>>>>>>> 7134550b09d6001d5ce347aaf8ec256d3aec77af
    private Integer id;
    private Integer organizationId;
    private Integer supplierOrderId;
    private Float quantity;
    private LocalDateTime shipmentStartingDate;
    private LocalDateTime estimatedArrivalDate;
    private LocalDateTime arrivalDate;
<<<<<<< HEAD
    private String status;
    private Integer sourceLocationId;
    private Integer destinationLocationId;
=======
    private ShipmentStatus status;
    private Location sourceLocation;
    private Location destinationLocation;
>>>>>>> 7134550b09d6001d5ce347aaf8ec256d3aec77af
    private Float currentLocationLatitude;
    private Float currentLocationLongitude;
}
