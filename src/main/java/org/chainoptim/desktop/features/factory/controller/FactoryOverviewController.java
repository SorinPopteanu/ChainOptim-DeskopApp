package org.chainoptim.desktop.features.factory.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class FactoryOverviewController implements DataReceiver<Factory> {

    private Factory factory;

    @FXML
    private Label factoryName;

    @Override
    public void setData(Factory factory) {
        this.factory = factory;
        factoryName.setText(factory.getName());
        System.out.println("Factory received in overview: " + factory.getName());
    }
}
