package org.chainoptim.desktop.features.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class WarehouseOverviewController implements DataReceiver<Warehouse> {
    private Warehouse warehouse;

    @FXML
    private Label warehouseName;

    @Override
    public void setData(Warehouse warehouse) {
        this.warehouse = warehouse;
        warehouseName.setText(warehouse.getName());
        System.out.println("Warehouse received in overview: " + warehouse.getName());
    }
}
