package org.chainoptim.desktop.features.supplier.controller;

import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class SupplierShipmentsController implements DataReceiver<SearchData<Supplier>> {

    @Override
    public void setData(SearchData<Supplier> searchData) {
        System.out.println("Supplier received in shipments: " + searchData.getData().getName());
    }
}
