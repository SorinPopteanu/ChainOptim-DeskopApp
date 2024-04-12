package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.util.DataReceiver;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AddProductionRecordController implements DataReceiver<Factory> {

    private Factory factory;

    @FXML
    private Label factoryNameLabel;

    @Override
    public void setData(Factory factory) {
        this.factory = factory;
        factoryNameLabel.setText(factory.getName());
    }

}
