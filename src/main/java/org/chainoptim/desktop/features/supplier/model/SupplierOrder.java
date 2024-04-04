package org.chainoptim.desktop.features.supplier.model;

import org.chainoptim.desktop.features.productpipeline.model.Component;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierOrder {

    public enum Status {
        Initiated,
        Negociated,
        Placed,
        Delivered
    }

    private Integer id;
    private Integer supplierId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Component component;
    private Integer organizationId;
    private Float quantity;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime deliveryDate;
    private String companyId;
    private Status status;
}
