package org.chainoptim.desktop.core.settings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettings {

    private boolean supplierOrdersOn;
    private boolean clientOrdersOn;
    private boolean factoryInventoryOn;
    private boolean warehouseInventoryOn;

    public NotificationSettings deepCopy() {
        return new NotificationSettings(supplierOrdersOn, clientOrdersOn, factoryInventoryOn, warehouseInventoryOn);
    }
}
