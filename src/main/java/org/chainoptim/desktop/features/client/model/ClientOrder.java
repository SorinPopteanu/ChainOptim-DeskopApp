package org.chainoptim.desktop.features.client.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientOrder {

    private Integer id;
    private Integer clientId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer productId;
    private Integer organizationId;
    private Float quantity;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime deliveryDate;
    private String status;
}
