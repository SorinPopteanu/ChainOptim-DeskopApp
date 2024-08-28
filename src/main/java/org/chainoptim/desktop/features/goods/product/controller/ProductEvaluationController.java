package org.chainoptim.desktop.features.goods.product.controller;

import org.chainoptim.desktop.features.goods.product.model.Product;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class ProductEvaluationController implements DataReceiver<Product> {

    private Product product;

    @Override
    public void setData(Product product) {
        this.product = product;
        System.out.println("Product received in evaluation: " + product.getName());
    }
}
