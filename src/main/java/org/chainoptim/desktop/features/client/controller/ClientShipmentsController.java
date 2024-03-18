package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class ClientShipmentsController implements DataReceiver<Client> {

    private Client client;

    @Override
    public void setData(Client client) {
        this.client = client;
        System.out.println("Client received in shipments: " + client.getName());
    }
}
