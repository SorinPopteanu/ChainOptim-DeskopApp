package org.chainoptim.desktop.features.product.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class ProductOverviewController implements DataReceiver<Product> {

    private Product product;

    @FXML
    private Label productName;

    @Override
    public void setData(Product product) {
        this.product = product;
//        productName.setText(product.getName());
        System.out.println("Product received in overview: " + product.getName());
    }
}
