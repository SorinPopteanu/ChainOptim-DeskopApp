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
import org.chainoptim.desktop.shared.table.edit.cells.EditableSpinnerTableCell;
import org.chainoptim.desktop.shared.table.edit.cells.EditableTextFieldTableCell;
import org.chainoptim.desktop.shared.table.model.TableData;
import org.chainoptim.desktop.shared.table.util.TableConfigurer;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.util.converter.FloatStringConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierOrdersController implements DataReceiver<Supplier> {

    // Services
    private final SupplierOrdersService supplierOrdersService;
    private final CurrentSelectionService currentSelectionService;
    private final NavigationServiceImpl navigationService;
    private final CommonViewsLoader commonViewsLoader;

    // Controllers
    private TableToolbarController tableToolbarController;
    private PageSelectorController pageSelectorController;

    // State
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;
    private Supplier supplier;
    private final SimpleIntegerProperty selectedCount = new SimpleIntegerProperty(0);
    private long totalRowsCount;
    private final List<TextFieldTableCell<SupplierOrder, ?>> editedCells = new ArrayList<>();

    // FXML
    @FXML
    private StackPane tableToolbarContainer;
    @FXML
    private ScrollPane supplierOrdersScrollPane;
    @FXML
    private TableView<TableData<SupplierOrder>> tableView;
    @FXML
    private TableColumn<TableData<SupplierOrder>, Boolean> selectRowColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, Integer> orderIdColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, Integer> supplierIdColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, Integer> componentIdColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, String> quantityColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, String> statusColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, LocalDateTime> orderDateColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, LocalDateTime> estimatedDeliveryDateColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, LocalDateTime> deliveryDateColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, String> companyIdColumn;
    @FXML
    private StackPane pageSelectorContainer;


    @Inject
    public SupplierOrdersController(SupplierOrdersService supplierOrdersService,
                                    NavigationServiceImpl navigationService,
                                    CurrentSelectionService currentSelectionService,
                                    CommonViewsLoader commonViewsLoader,
                                    FallbackManager fallbackManager,
                                    SearchParams searchParams) {
        this.supplierOrdersService = supplierOrdersService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void setData(Supplier supplier) {
        this.supplier = supplier;
        TableConfigurer.configureTableView(tableView, selectRowColumn);
        bindDataToTableView();
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
        tableToolbarController = commonViewsLoader.initializeTableToolbar(tableToolbarContainer);
        tableToolbarController.initialize(() -> loadSupplierOrders(this.supplier.getId()));

        setUpListeners();

        loadSupplierOrders(this.supplier.getId());
    }

    private void bindDataToTableView() {

        selectRowColumn.setCellValueFactory(data -> data.getValue().isSelectedProperty());
        orderIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getId()));
        supplierIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getSupplierId()));
        componentIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getComponent().getId()));
        quantityColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        Float.toString(data.getValue().getData().getQuantity() != null ? data.getValue().getData().getQuantity() : 0.0f)
                )
        );

        statusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getStatus() != null ? data.getValue().getData().getStatus().name() : "N/A"));
        orderDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getOrderDate()));
        estimatedDeliveryDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getEstimatedDeliveryDate()));
        deliveryDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getDeliveryDate()));
        companyIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getCompanyId()).asString());

        quantityColumn.setCellFactory(col -> new EditableTextFieldTableCell<SupplierOrder>());
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

    private void loadSupplierOrders(Integer supplierId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            fallbackManager.setLoading(false);
            return;
        }

        supplierOrdersService.getSuppliersBySupplierIdAdvanced(supplierId, searchParams)
                .thenApply(this::handleOrdersResponse)
                .exceptionally(this::handleOrdersException);
    }

    private Optional<PaginatedResults<SupplierOrder>> handleOrdersResponse(Optional<PaginatedResults<SupplierOrder>> supplierOrdersOptional) {
        Platform.runLater(() -> {
            if (supplierOrdersOptional.isEmpty()) {
                fallbackManager.setErrorMessage("No orders found");
                return;
            }
            PaginatedResults<SupplierOrder> paginatedResults = supplierOrdersOptional.get();
            fallbackManager.setLoading(false);

            totalRowsCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalRowsCount);

            tableView.getItems().clear();
            if (paginatedResults.results.isEmpty()) {
                fallbackManager.setNoResults(true);
                return;
            }

            for (SupplierOrder supplierOrder : paginatedResults.results) {
                TableData<SupplierOrder> tableRow = new TableData<>(supplierOrder, new SimpleBooleanProperty(false));
                setRowListeners(tableRow);
            }
        });

        return supplierOrdersOptional;
    }

    private Optional<PaginatedResults<SupplierOrder>> handleOrdersException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supplier orders."));
        return Optional.empty();
    }

    private void setRowListeners(TableData<SupplierOrder> supplierOrder) {
        // Add listener to the selectedProperty of SupplierOrder
        supplierOrder.isSelectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (Boolean.TRUE.equals(isSelected)) {
                selectedCount.set(selectedCount.get() + 1);
            } else {
                selectedCount.set(selectedCount.get() - 1);
            }
        });
        selectRowColumn.textProperty().bind(selectedCount.asString());

        tableView.getItems().add(supplierOrder);
    }

    public void deselectAllRows() {
        tableToolbarController.getSaveChangesButton().setVisible(false);
        tableToolbarController.getSaveChangesButton().setManaged(false);
        selectRowColumn.setEditable(true);
        tableView.getItems().forEach(order -> order.setSelectedProperty(new SimpleBooleanProperty(false)));
        for (TextFieldTableCell<SupplierOrder, ?> cell : editedCells) {
            cell.cancelEdit();
        }
    }


    public void editSelectedRows() {
        tableToolbarController.getEditSelectedRowsButton().setVisible(false);
        tableToolbarController.getEditSelectedRowsButton().setManaged(false);
        tableToolbarController.getSaveChangesButton().setVisible(true);
        tableToolbarController.getSaveChangesButton().setManaged(true);
        selectRowColumn.setEditable(false);
//        quantityColumn.setCellFactory(col -> new EditableSpinnerTableCell<>(new FloatStringConverter()));
//        quantityColumn.setCellFactory(column -> new TextFieldTableCell<SupplierOrder, Float>(new FloatStringConverter()) {
//            private TextField textField;
//            private Float initialValue;
//            private ChangeListener<Boolean> editListener;
//
//            @Override
//            public void updateItem(Float item, boolean empty) {
//                super.updateItem(item, empty);
//                if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
//                    SupplierOrder order = getTableView().getItems().get(getIndex());
//
//                    if (editListener != null) {
//                        order.selectedProperty().removeListener(editListener);
//                    }
//
//                    if (empty) {
//                        setGraphic(null);
//                    } else {
//                        editListener = (obs, wasSelected, isSelected) -> {
//                            if (isSelected) {
//                                textField = new TextField();
//                                textField.setEditable(true);
//                                textField.setText(item != null ? item.toString() : "");
//                                setGraphic(textField);
//                            } else {
//                                setGraphic(null);
//                            }
//                        };
//                    }
//                    order.selectedProperty().addListener(editListener);
//                    editListener.changed(null, !order.selectedProperty().get(), order.selectedProperty().get());
//                }
//            }
//
//            @Override
//            public void startEdit() {
//                TableData<SupplierOrder> order = getTableView().getItems().get(getIndex());
//                if (order.isSelectedProperty().get()) {
//                    initialValue = super.getItem();
//                    super.startEdit();
//                    editedCells.add(this);
//                }
//            }
//
//            @Override
//            public void cancelEdit() {
//                super.cancelEdit();
//                super.setGraphic(null);
//                super.setItem(initialValue);
//                SupplierOrder order = getTableView().getItems().get(getIndex());
//                if (editListener != null) {
//                    order.selectedProperty().removeListener(editListener);
//                }
//                editedCells.remove(this);
//            }
//        });
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

