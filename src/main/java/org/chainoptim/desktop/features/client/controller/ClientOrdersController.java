package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.features.client.service.ClientOrdersService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import com.google.inject.Inject;

import java.time.LocalDateTime;
import java.util.Collections;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.List;
import java.util.Optional;

public class ClientOrdersController implements DataReceiver<Client> {

    private final ClientOrdersService clientOrdersService;
    private final FallbackManager fallbackManager;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    private Client client;
    private List<ClientOrder> clientOrders;

    @FXML
    private StackPane stackPane;
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
    private StackPane fallbackContainer;

    @Inject
    public ClientOrdersController(FallbackManager fallbackManager,
                                  ClientOrdersService clientOrdersService,
                                  FXMLLoaderService fxmlLoaderService,
                                  ControllerFactory controllerFactory) {
        this.fallbackManager = fallbackManager;
        this.clientOrdersService = clientOrdersService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void setData(Client client) {
        loadFallbackManager();
        this.client = client;
        loadClientOrders(client.getId());
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void loadClientOrders(Integer clientId) {
        fallbackManager.reset();
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
            configureTableView();
            bindDataToTableView();
            setEditEvents();
        });

        return clientOrders;
    }

    private void configureTableView() {
        tableView.setEditable(true);
        clientIdColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        productIdColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        statusColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        tableView.setMaxHeight(Double.MAX_VALUE);

        // Set the column resize policy and their minimum width
        tableView.setColumnResizePolicy(new Callback<TableView.ResizeFeatures, Boolean>() {
            @Override
            public Boolean call(TableView.ResizeFeatures param) {
                return TableView.CONSTRAINED_RESIZE_POLICY.call(param) || Boolean.TRUE;
            }
        });
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

        tableView.getItems().setAll(clientOrders);
    }

    private void setEditEvents() {
        clientIdColumn.setOnEditCommit(event -> {
            ClientOrder order = event.getRowValue();
            order.setClientId(event.getNewValue());
            updateInDatabase(order);
        });

        productIdColumn.setOnEditCommit(event -> {
            ClientOrder order = event.getRowValue();
            order.setProductId(event.getNewValue());
            updateInDatabase(order);
        });

        quantityColumn.setOnEditCommit(event -> {
            ClientOrder order = event.getRowValue();
            order.setQuantity(event.getNewValue());
            updateInDatabase(order);
        });

        statusColumn.setOnEditCommit(event -> {
            ClientOrder order = event.getRowValue();
            order.setStatus(event.getNewValue());
            updateInDatabase(order);
        });
    }

    private void updateInDatabase(ClientOrder order) {
        System.out.println("Change the database with the new value: " + order);
    }

    private List<ClientOrder> handleOrdersException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load client orders."));
        return Collections.emptyList();
    }
}
