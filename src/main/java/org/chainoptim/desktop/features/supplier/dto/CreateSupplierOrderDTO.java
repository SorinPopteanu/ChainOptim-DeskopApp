package org.chainoptim.desktop.features.supplier.dto;

import lombok.Data;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
import org.chainoptim.desktop.shared.enums.OrderStatus;

import java.time.LocalDateTime;

@Data
public class CreateSupplierOrderDTO {

    private Integer supplierId;
    private Integer organizationId;
    private Integer componentId;
    private Float quantity;
    private Float deliveredQuantity;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime deliveryDate;
    private OrderStatus status;
    private String companyId;
}
