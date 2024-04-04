package org.chainoptim.desktop.features.supplier.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
import org.chainoptim.desktop.features.supplier.service.SupplierOrdersService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParamsImpl;
import org.chainoptim.desktop.shared.table.TableToolbarController;
import org.chainoptim.desktop.shared.table.edit.cells.EditableSpinnerTableCell;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierOrdersController implements DataReceiver<Supplier> {

    private final SupplierOrdersService supplierOrdersService;
    private final FallbackManager fallbackManager;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final CurrentSelectionService currentSelectionService;
    private final NavigationServiceImpl navigationService;
    private final SearchParamsImpl searchParams;

    private Supplier supplier;
    private SimpleIntegerProperty selectedCount = new SimpleIntegerProperty(0);


    @FXML
    private StackPane tableToolbarContainer;
    @FXML
    private TableToolbarController tableToolbarController;
    @FXML
    private ScrollPane supplierOrdersScrollPane;
    @FXML
    private TableView<SupplierOrder> tableView;
    @FXML
    private TableColumn<SupplierOrder, Boolean> selectRowColumn;
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
    @FXML
    private TableColumn<SupplierOrder, String> companyIdColumn;
    @FXML
    private StackPane pageSelectorContainer;
    @FXML
    private PageSelectorController pageSelectorController;

    private long totalRowsCount;

    private List<TextFieldTableCell<SupplierOrder, ?>> cells = new ArrayList<>();


    @Inject
    public SupplierOrdersController(SupplierOrdersService supplierOrdersService,
                                    NavigationServiceImpl navigationService,
                                    CurrentSelectionService currentSelectionService,
                                    FallbackManager fallbackManager,
                                    FXMLLoaderService fxmlLoaderService,
                                    ControllerFactory controllerFactory,
                                    SearchParamsImpl searchParams) {
        this.supplierOrdersService = supplierOrdersService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.searchParams = searchParams;

    }

    @Override
    public void setData(Supplier supplier) {
        this.supplier = supplier;
        loadSupplierOrders(this.supplier);
        configureTableView();
        initializeTableToolbar();
        initializePageSelector();
        setEditEvents();
        setUpListeners();
    }

    private void initializeTableToolbar() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/table/TableToolbarView.fxml",
                controllerFactory::createController
        );
        try {
            Node tableToolbarView = loader.load();
            tableToolbarContainer.getChildren().add(tableToolbarView);
            tableToolbarController = loader.getController();
            tableToolbarController.initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializePageSelector() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/search/PageSelectorView.fxml",
                controllerFactory::createController
        );
        try {
            Node pageSelectorView = loader.load();
            pageSelectorContainer.getChildren().add(pageSelectorView);
            pageSelectorController = loader.getController();
            searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadSupplierOrders(supplier));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSupplierOrders(Supplier supplier) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        System.out.println("Current page: " + searchParams.getPage());

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }
        Integer supplierId = supplier.getId();
        System.out.println("Supplier ID: " + supplierId);
        supplierOrdersService.getSuppliersBySupplierIdAdvanced(supplierId, searchParams)
                .thenApply(this::handleOrdersResponse)
                .exceptionally(this::handleOrdersException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<PaginatedResults<SupplierOrder>> handleOrdersResponse(Optional<PaginatedResults<SupplierOrder>> supplierOrdersOptional) {
        Platform.runLater(() -> {
            if (supplierOrdersOptional.isEmpty()) {
                fallbackManager.setErrorMessage("No orders found");
                return;
            }
            PaginatedResults<SupplierOrder> paginatedResults = supplierOrdersOptional.get();
            tableView.getItems().clear();
            totalRowsCount = paginatedResults.getTotalCount();

            if (!paginatedResults.results.isEmpty()) {
                for (SupplierOrder supplierOrder : paginatedResults.results) {
                    bindDataToTableView(supplierOrder);
                }
                Platform.runLater(() -> pageSelectorController.initialize(searchParams, totalRowsCount));

                fallbackManager.setNoResults(false);
            } else {
                fallbackManager.setNoResults(true);
            }
        });

        return supplierOrdersOptional;
    }

    private Optional<PaginatedResults<SupplierOrder>> handleOrdersException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supplier orders."));
        return Optional.empty();
    }

    private void configureTableView() {
        tableView.setMaxHeight(Double.MAX_VALUE);
        tableView.setEditable(true);
        tableView.getColumns().forEach(column -> column.setEditable(false));

        selectRowColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectRowColumn));
        selectRowColumn.setEditable(true);

        quantityColumn.setCellFactory(col -> new EditableSpinnerTableCell<>(new FloatStringConverter()));

        tableView.setColumnResizePolicy(new Callback<TableView.ResizeFeatures, Boolean>() {
            @Override
            public Boolean call(TableView.ResizeFeatures param) {
                return TableView.CONSTRAINED_RESIZE_POLICY.call(param) || Boolean.TRUE;
            }
        });
    }

    private void bindDataToTableView(SupplierOrder results) {
        selectRowColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        orderIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        supplierIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSupplierId()));
        componentIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getComponentId()));
        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantity()));
        statusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStatus() != null ? data.getValue().getStatus().name() : "N/A"));
        orderDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getOrderDate()));
        estimatedDeliveryDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEstimatedDeliveryDate()));
        deliveryDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDeliveryDate()));
        companyIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCompanyId()).asString());

        // Add listener to the selectedProperty of SupplierOrder
        results.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                selectedCount.set(selectedCount.get() + 1);
            } else {
                selectedCount.set(selectedCount.get() - 1);
            }
        });
        selectRowColumn.textProperty().bind(selectedCount.asString());

        tableView.getItems().add(results);
    }

    private void setUpListeners() {
        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            supplierOrdersScrollPane.setVisible(newValue);
            supplierOrdersScrollPane.setManaged(newValue);
        });

        // Add listener to selectedCount property
        selectedCount.addListener((obs, oldCount, newCount) -> {
            boolean isAnyRowSelected = newCount.intValue() > 0;
            tableToolbarController.setButtonsAvailability(isAnyRowSelected);
        });

        // Add listeners to the toolbar buttons
        tableToolbarController.getCancelRowSelectionButton().setOnAction(e -> deselectAllRows());
        tableToolbarController.getEditSelectedRowsButton().setOnAction(e -> editSelectedRows());

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

    public void deselectAllRows() {
        tableToolbarController.getSaveChangesButton().setVisible(false);
        selectRowColumn.setEditable(true);
        tableView.getItems().forEach(order -> order.setSelectedProperty(false));
        for (TextFieldTableCell<SupplierOrder, ?> cell : cells) {
            cell.cancelEdit();
        }
    }

    public void editSelectedRows() {
        tableToolbarController.getEditSelectedRowsButton().setVisible(false);
        tableToolbarController.getSaveChangesButton().setVisible(true);
        selectRowColumn.setEditable(false);

        
        quantityColumn.setCellFactory(column -> new TextFieldTableCell<SupplierOrder, Float>(new FloatStringConverter()) {
            private TextField textField;
            private Float initialValue;
            private ChangeListener<Boolean> editListener;

            @Override
            public void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                    SupplierOrder order = getTableView().getItems().get(getIndex());

                    if (editListener != null) {
                        order.selectedProperty().removeListener(editListener);
                    }

                    if (empty) {
                        setGraphic(null);
                    } else {
                        editListener = (obs, wasSelected, isSelected) -> {
                            if (isSelected) {
                                textField = new TextField();
                                textField.setEditable(true);
                                textField.setText(item != null ? item.toString() : "");
                                setGraphic(textField);
                            } else {
                                setGraphic(null);
                            }
                        };
                    }
                    order.selectedProperty().addListener(editListener);
                    editListener.changed(null, !order.selectedProperty().get(), order.selectedProperty().get());
                }
            }

            @Override
            public void startEdit() {
                SupplierOrder order = getTableView().getItems().get(getIndex());
                if (order.selectedProperty().get()) {
                    initialValue = getItem();
                    super.startEdit();
                    cells.add(this);
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setGraphic(null);
                setItem(initialValue);
                SupplierOrder order = getTableView().getItems().get(getIndex());
                if (editListener != null) {
                    order.selectedProperty().removeListener(editListener);
                }
                cells.remove(this);
            }
        });
        quantityColumn.setEditable(true);

//        quantityColumn.setCellFactory(column -> new EditableSpinnerTableCell<SupplierOrder, Float>(new FloatStringConverter()));
//        tableView.getSelectionModel().getSelectedItems().forEach(item -> {
//            if (item != null) {
//                int index = tableView.getItems().indexOf(item);
//                if (index >= 0) {
//                    tableView.edit(index, quantityColumn);
//                    quantityColumn.setEditable(true);
//                }
//            }
//        });
//        quantityColumn.setEditable(true);


    }
}

