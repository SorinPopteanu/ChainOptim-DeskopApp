package org.chainoptim.desktop.features.factory.controller;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class FactoryInventoryController implements DataReceiver<Factory> {

    private Factory factory;

    @Override
    public void setData(Factory factory) {
        this.factory = factory;
        System.out.println("Factory received in inventory: " + factory.getName());
    }
}
