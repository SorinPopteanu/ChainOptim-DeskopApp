package org.chainoptim.desktop.features.product.controller;

import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class ProductProductionController implements DataReceiver<Product> {

    private Product product;

    @Override
    public void setData(Product product) {
        this.product = product;
        System.out.println("Product received in production: " + product.getName());
    }
}
