package org.chainoptim.desktop.features.supplier.dto;

import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
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
        private LocalDateTime orderDate;
        private LocalDateTime estimatedDeliveryDate;
        private LocalDateTime deliveryDate;
        private OrderStatus status;
        private String companyId;

        @Override
        public String toString() {
                return "UpdateSupplierOrderDTO{" +
                        "id=" + id +
                        ", organizationId=" + organizationId +
                        ", componentId=" + componentId +
                        ", quantity=" + quantity +
                        ", orderDate=" + orderDate +
                        ", estimatedDeliveryDate=" + estimatedDeliveryDate +
                        ", deliveryDate=" + deliveryDate +
                        ", status=" + status +
                        ", companyId='" + companyId + '\'' +
                        '}';
        }
}
