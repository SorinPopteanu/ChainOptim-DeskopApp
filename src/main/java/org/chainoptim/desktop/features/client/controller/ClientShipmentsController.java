package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class ClientShipmentsController implements DataReceiver<SearchData<Client>> {

    private Client client;

    @Override
    public void setData(SearchData<Client> searchData) {
        this.client = searchData.getData();
        System.out.println("Client received in shipments: " + client.getName());
    }
}
