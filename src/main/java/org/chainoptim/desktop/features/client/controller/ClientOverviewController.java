package org.chainoptim.desktop.features.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class ClientOverviewController implements DataReceiver<SearchData<Client>> {

    private Client client;

    @FXML
    private Label clientName;

    @Override
    public void setData(SearchData<Client> searchData) {
        this.client = searchData.getData();
        clientName.setText(client.getName());
        System.out.println("Client received in overview: " + client.getName());
    }
}
