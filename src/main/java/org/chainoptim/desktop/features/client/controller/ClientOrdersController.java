package org.chainoptim.desktop.features.client.controller;

import com.google.inject.Inject;

import java.time.LocalDateTime;
import java.util.Collections;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.features.client.service.ClientOrdersService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;

import java.util.List;
import java.util.Optional;

public class ClientOrdersController implements DataReceiver<Client> {

    private final ClientOrdersService clientOrdersService;
    private final FallbackManager fallbackManager;

    private Client client;
    private List<ClientOrder> clientOrders;

    @FXML
    private TableView<ClientOrder> tableView;
    @FXML
    private TableColumn<ClientOrder, Integer> orderIdColumn;
    @FXML
    private TableColumn<ClientOrder, Integer> clientIdColumn;
    @FXML
    private TableColumn<ClientOrder, Integer> productIdColumn;
    @FXML
    private TableColumn<ClientOrder, Float> quantityColumn;
    @FXML
    private TableColumn<ClientOrder, String> statusColumn;
    @FXML
    private TableColumn<ClientOrder, LocalDateTime> orderDateColumn;
    @FXML
    private TableColumn<ClientOrder, LocalDateTime> estimatedDeliveryDateColumn;
    @FXML
    private TableColumn<ClientOrder, LocalDateTime> deliveryDateColumn;

    @FXML
    private Label clientName;

    @Inject
    public ClientOrdersController(FallbackManager fallbackManager,
                                  ClientOrdersService clientOrdersService) {
        this.fallbackManager = fallbackManager;
        this.clientOrdersService = clientOrdersService;
    }

    @Override
    public void setData(Client client) {
        this.client = client;
        this.clientName.setText(client.getName());
        System.out.println("Client received in orders: " + client.getName());

        loadClientOrders(client.getId());
    }

    private void loadClientOrders(Integer clientId) {
        fallbackManager.setLoading(true);

        clientOrdersService.getClientOrdersByOrganizationId(clientId)
                .thenApply(this::handleOrdersResponse)
                .exceptionally(this::handleOrdersException);
    }

    private List<ClientOrder> handleOrdersResponse(Optional<List<ClientOrder>> orders) {
        Platform.runLater(() -> {
            if (orders.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load client orders.");
                return;
            }
            this.clientOrders = orders.get();
            fallbackManager.setLoading(false);
            System.out.println("Orders received: " + clientOrders);
            bindDataToTableView();
        });

        return clientOrders;
    }

    private void bindDataToTableView() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        clientIdColumn.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        estimatedDeliveryDateColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedDeliveryDate"));
        deliveryDateColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));

        // Set the items in the table
        tableView.getItems().setAll(clientOrders);

    }

    private List<ClientOrder> handleOrdersException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load client orders."));
        return Collections.emptyList();
    }
}