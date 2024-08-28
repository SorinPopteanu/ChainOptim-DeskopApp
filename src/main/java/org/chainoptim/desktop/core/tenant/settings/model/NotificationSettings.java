package org.chainoptim.desktop.core.tenant.settings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettings {

    private boolean supplierOrdersOn;
    private boolean supplierShipmentsOn;
    private boolean clientOrdersOn;
    private boolean clientShipmentsOn;
    private boolean factoryInventoryOn;
    private boolean warehouseInventoryOn;
    private boolean emailSupplierOrdersOn;
    private boolean emailSupplierShipmentsOn;
    private boolean emailClientOrdersOn;
    private boolean emailClientShipmentsOn;
    private boolean emailFactoryInventoryOn;
    private boolean emailWarehouseInventoryOn;

    public NotificationSettings deepCopy() {
        return new NotificationSettings(
                supplierOrdersOn, supplierShipmentsOn, clientOrdersOn, clientShipmentsOn, factoryInventoryOn, warehouseInventoryOn,
                emailSupplierOrdersOn, emailSupplierShipmentsOn, emailClientOrdersOn, emailClientShipmentsOn, emailFactoryInventoryOn, emailWarehouseInventoryOn);
    }
}
