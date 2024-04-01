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
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.table.TableToolbarController;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import java.time.LocalDateTime;
import java.util.Optional;

public class SupplierOrdersController implements DataReceiver<Supplier> {

    private final SupplierOrdersService supplierOrdersService;
    private final FallbackManager fallbackManager;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final CurrentSelectionService currentSelectionService;
    private final NavigationServiceImpl navigationService;
    private final SearchParams searchParams;

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
    private StackPane pageSelectorContainer;
    @FXML
    private PageSelectorController pageSelectorController;

    private long totalRowsCount;
    private ObservableList<SupplierOrder.Status> statusList = FXCollections.observableArrayList(SupplierOrder.Status.values());

    @Inject
    public SupplierOrdersController(SupplierOrdersService supplierOrdersService,
                                    NavigationServiceImpl navigationService,
                                    CurrentSelectionService currentSelectionService,
                                    FallbackManager fallbackManager,
                                    FXMLLoaderService fxmlLoaderService,
                                    ControllerFactory controllerFactory,
                                    SearchParams searchParams) {
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
        System.out.println("Supplier: " + this.supplier);
        initializeTableToolbar();
        loadSupplierOrders(this.supplier);
        initializePageSelector();
        configureTableView();
        setUpListeners();
        setEditEvents();
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
        System.out.println("Received supplier orders: " + supplierOrdersOptional);

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
                pageSelectorController.initialize(totalRowsCount);
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
        tableView.getItems().forEach(order -> order.setSelected(false));
    }


}

