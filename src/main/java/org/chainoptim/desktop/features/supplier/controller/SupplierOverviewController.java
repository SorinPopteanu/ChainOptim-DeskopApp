package org.chainoptim.desktop.features.supplier.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class SupplierOverviewController implements DataReceiver<SearchData<Supplier>> {

    private Supplier supplier;

    @FXML
    private Label supplierName;

    @Override
    public void setData(SearchData<Supplier> searchData) {
        this.supplier = searchData.getData();
        supplierName.setText(supplier.getName());
        System.out.println("Supplier received in overview: " + supplier.getName());
    }

}
