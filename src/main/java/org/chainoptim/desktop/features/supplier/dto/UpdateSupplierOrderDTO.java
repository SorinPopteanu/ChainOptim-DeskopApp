package org.chainoptim.desktop.features.supplier.dto;

import org.chainoptim.desktop.shared.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateSupplierOrderDTO {
        private Integer id;
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
