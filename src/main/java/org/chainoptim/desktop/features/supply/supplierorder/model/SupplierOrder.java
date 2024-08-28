package org.chainoptim.desktop.features.supply.supplierorder.model;

import org.chainoptim.desktop.features.goods.component.model.Component;
import org.chainoptim.desktop.shared.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierOrder {

    public SupplierOrder(SupplierOrder data) {
        this.id = data.id;
        this.supplierId = data.supplierId;
        this.createdAt = data.createdAt;
        this.updatedAt = data.updatedAt;
        this.component = data.component;
        this.organizationId = data.organizationId;
        this.quantity = data.quantity;
        this.deliveredQuantity = data.deliveredQuantity;
        this.orderDate = data.orderDate;
        this.estimatedDeliveryDate = data.estimatedDeliveryDate;
        this.deliveryDate = data.deliveryDate;
        this.companyId = data.companyId;
        this.status = data.status;
    }

    private Integer id;
    private Integer supplierId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Component component;
    private Integer organizationId;
    private Float quantity;
    private Float deliveredQuantity;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime deliveryDate;
    private String companyId;
    private OrderStatus status;
}
