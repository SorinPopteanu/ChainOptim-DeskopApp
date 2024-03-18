package org.chainoptim.desktop.features.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class ClientOverviewController implements DataReceiver<Client> {

    private Client client;

    @FXML
    private Label clientName;

    @Override
    public void setData(Client client) {
        this.client = client;
        clientName.setText(client.getName());
        System.out.println("Client received in overview: " + client.getName());
    }
}
