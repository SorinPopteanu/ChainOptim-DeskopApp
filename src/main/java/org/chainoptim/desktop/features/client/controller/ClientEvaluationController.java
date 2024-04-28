package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.util.DataReceiver;

import java.util.List;

public class ClientEvaluationController implements DataReceiver<SearchData<Client>> {

    private Client client;

    @Override
    public void setData(SearchData<Client> searchData) {
        this.client = searchData.getData();
        System.out.println("Client received in evaluation: " + client.getName());
    }

//    private void load
}
