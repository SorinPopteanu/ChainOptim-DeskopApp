package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class WarehouseInventoryController implements DataReceiver<Warehouse> {

    private Warehouse warehouse;
    @Override
    public void setData(Warehouse warehouse) {
        System.out.println("Warehouse received in orders: " + warehouse.getName());
    }

}
