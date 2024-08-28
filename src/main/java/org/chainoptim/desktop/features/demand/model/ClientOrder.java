package org.chainoptim.desktop.features.demand.model;


import org.chainoptim.desktop.features.goods.product.model.Product;
import org.chainoptim.desktop.shared.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientOrder {

    public ClientOrder(ClientOrder clientOrder) {
        this.id = clientOrder.id;
        this.clientId = clientOrder.clientId;
        this.createdAt = clientOrder.createdAt;
        this.updatedAt = clientOrder.updatedAt;
        this.product = clientOrder.product;
        this.organizationId = clientOrder.organizationId;
        this.quantity = clientOrder.quantity;
        this.deliveredQuantity = clientOrder.deliveredQuantity;
        this.orderDate = clientOrder.orderDate;
        this.estimatedDeliveryDate = clientOrder.estimatedDeliveryDate;
        this.deliveryDate = clientOrder.deliveryDate;
        this.status = clientOrder.status;
        this.companyId = clientOrder.companyId;
    }

    private Integer id;
    private Integer clientId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Product product;
    private Integer organizationId;
    private Float quantity;
    private Float deliveredQuantity;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime deliveryDate;
    private OrderStatus status;
    private String companyId;
}
