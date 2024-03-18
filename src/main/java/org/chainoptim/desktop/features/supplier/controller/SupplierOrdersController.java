package org.chainoptim.desktop.features.supplier.controller;

import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class SupplierOrdersController implements DataReceiver<Supplier> {
    @Override
    public void setData(Supplier supplier) {
        System.out.println("Supplier received in orders: " + supplier.getName());
    }
}
