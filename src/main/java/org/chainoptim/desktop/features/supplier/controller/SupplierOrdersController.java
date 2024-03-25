package org.chainoptim.desktop.features.supplier.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
import org.chainoptim.desktop.features.supplier.service.SupplierOrdersService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SupplierOrdersController implements DataReceiver<Supplier> {

    private final SupplierOrdersService supplierOrdersService;
    private final FallbackManager fallbackManager;
    private List<SupplierOrder> supplierOrders;
    private Supplier supplier;

    @FXML
    private StackPane stackPane;
    @FXML
    private TableView<SupplierOrder> tableView;
    @FXML
    private TableColumn<SupplierOrder, Integer> orderIdColumn;
    @FXML
    private TableColumn<SupplierOrder, Integer> supplierIdColumn;
    @FXML
    private TableColumn<SupplierOrder, Integer> componentIdColumn;
    @FXML
    private TableColumn<SupplierOrder, Float> quantityColumn;
    @FXML
    private TableColumn<SupplierOrder, String> statusColumn;
    @FXML
    private TableColumn<SupplierOrder, LocalDateTime> orderDateColumn;
    @FXML
    private TableColumn<SupplierOrder, LocalDateTime> estimatedDeliveryDateColumn;
    @FXML
    private TableColumn<SupplierOrder, LocalDateTime> deliveryDateColumn;

    @Inject
    public SupplierOrdersController(SupplierOrdersService supplierOrdersService,
                                    FallbackManager fallbackManager) {
        this.supplierOrdersService = supplierOrdersService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Supplier supplier) {
        this.supplier = supplier;
        loadSupplierOrders(supplier.getId());
    }

    private void loadSupplierOrders(Integer supplierId) {
        fallbackManager.setLoading(true);

        supplierOrdersService.getSupplierOrdersByOrganizationId(supplierId)
                .thenApply(this::handleOrdersResponse)
                .exceptionally(this::handleOrdersException);
    }

    private List<SupplierOrder> handleOrdersResponse(Optional<List<SupplierOrder>> orders) {
        Platform.runLater(() -> {
            if (orders.isEmpty()) {
                fallbackManager.setErrorMessage("No orders found");
                return;
            }
            this.supplierOrders = orders.get();
            fallbackManager.setLoading(false);
            System.out.println("Orders received: " + supplierOrders);
            setTableView();
            bindDataToTableView();
            setEditEvents();
        });

        return supplierOrders;
    }

    private void setTableView() {
        tableView.setEditable(true);
        supplierIdColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        componentIdColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        statusColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        tableView.setMaxHeight(Double.MAX_VALUE);

        tableView.setColumnResizePolicy(new Callback<TableView.ResizeFeatures, Boolean>() {
            @Override
            public Boolean call(TableView.ResizeFeatures param) {
                return TableView.CONSTRAINED_RESIZE_POLICY.call(param) || Boolean.TRUE;
            }
        });
    }

    private void bindDataToTableView() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        componentIdColumn.setCellValueFactory(new PropertyValueFactory<>("componentId"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        estimatedDeliveryDateColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedDeliveryDate"));
        deliveryDateColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));

        tableView.getItems().setAll(supplierOrders);
    }

    private void setEditEvents() {
        supplierIdColumn.setOnEditCommit(event -> {
            SupplierOrder order = event.getRowValue();
            order.setSupplierId(event.getNewValue());
            updateInDatabase(order);
        });

        componentIdColumn.setOnEditCommit(event -> {
            SupplierOrder order = event.getRowValue();
            order.setComponentId(event.getNewValue());
            updateInDatabase(order);
        });

        quantityColumn.setOnEditCommit(event -> {
            SupplierOrder order = event.getRowValue();
            order.setQuantity(event.getNewValue());
            updateInDatabase(order);
        });
    }

    private void updateInDatabase(SupplierOrder order) {
        System.out.println("Change detected: " + order);
    }

    private List<SupplierOrder> handleOrdersException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supplier orders."));
        return Collections.emptyList();
    }


}

