package org.chainoptim.desktop.features.supplier.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
import org.chainoptim.desktop.features.supplier.service.SupplierOrdersService;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.table.TableToolbarController;
import org.chainoptim.desktop.shared.table.edit.cells.EditableCell;
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SupplierOrdersController implements DataReceiver<Supplier> {

    // Services
    private final SupplierOrdersService supplierOrdersService;
    private final CurrentSelectionService currentSelectionService;
    private final NavigationServiceImpl navigationService;
    private final CommonViewsLoader commonViewsLoader;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    // Controllers
    private TableToolbarController tableToolbarController;
    private PageSelectorController pageSelectorController;
    private GenericConfirmDialogController<List<SupplierOrder>> confirmSupplierOrderUpdateController;
    private GenericConfirmDialogController<List<SupplierOrder>> confirmSupplierOrderDeleteController;

    // State
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;
    private long totalRowsCount;
    private Supplier supplier;
    private final List<Integer> selectedRowsIndices = new ArrayList<>();
    private final SimpleIntegerProperty selectedCount = new SimpleIntegerProperty(0);
    private final BooleanProperty isEditMode = new SimpleBooleanProperty(false);

    // Confirm Dialog Listeners
    private RunnableConfirmDialogActionListener<List<SupplierOrder>> confirmDialogUpdateListener;
    private RunnableConfirmDialogActionListener<List<SupplierOrder>> confirmDialogDeleteListener;

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
    private TableColumn<TableData<SupplierOrder>, String> companyIdColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, String> supplierNameColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, String> componentNameColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, Float> quantityColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, String> statusColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, LocalDateTime> orderDateColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, LocalDateTime> estimatedDeliveryDateColumn;
    @FXML
    private TableColumn<TableData<SupplierOrder>, LocalDateTime> deliveryDateColumn;
    @FXML
    private StackPane pageSelectorContainer;
    @FXML
    private StackPane confirmUpdateDialogContainer;
    @FXML
    private StackPane confirmDeleteDialogContainer;


    @Inject
    public SupplierOrdersController(SupplierOrdersService supplierOrdersService,
                                    NavigationServiceImpl navigationService,
                                    CurrentSelectionService currentSelectionService,
                                    CommonViewsLoader commonViewsLoader,
                                    FallbackManager fallbackManager,
                                    SearchParams searchParams,
                                    FXMLLoaderService fxmlLoaderService,
                                    ControllerFactory controllerFactory) {
        this.supplierOrdersService = supplierOrdersService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void setData(Supplier supplier) {
        this.supplier = supplier;
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
        tableToolbarController = commonViewsLoader.initializeTableToolbar(tableToolbarContainer);
        tableToolbarController.initialize(() -> loadSupplierOrders(supplier.getId()));

        TableConfigurer.configureTableView(tableView, selectRowColumn);
        configureTableColumns();
        setUpListeners();

        loadSupplierOrders(supplier.getId());
        loadConfirmDeleteDialog();
        loadConfirmDeleteDialog();
        confirmDeleteDialogContainer.setVisible(false);
        confirmUpdateDialogContainer.setVisible(false);
    }

    private void configureTableColumns() {
        // Bind columns to data
        selectRowColumn.setCellValueFactory(data -> data.getValue().isSelectedProperty());
        orderIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getId()));
        companyIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(
                data.getValue().getData().getCompanyId() != null ? data.getValue().getData().getCompanyId() : "N/A"
        ));
        supplierNameColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(this.supplier.getName()));
//        componentNameColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getComponent().getName()));
        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getQuantity()));
        statusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getStatus() != null ? data.getValue().getData().getStatus().name() : "N/A"));
        orderDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getOrderDate()));
        estimatedDeliveryDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getEstimatedDeliveryDate()));
        deliveryDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getDeliveryDate()));

        // Configure columns to use custom editable cells
        companyIdColumn.setCellFactory(column -> new EditableCell<>(isEditMode, selectedRowsIndices));
        quantityColumn.setCellFactory(column -> new EditableCell<>(isEditMode, selectedRowsIndices));
        estimatedDeliveryDateColumn.setCellFactory(column -> new EditableCell<>(isEditMode, selectedRowsIndices));
        deliveryDateColumn.setCellFactory(column -> new EditableCell<>(isEditMode, selectedRowsIndices));
    }

    private void setUpListeners() {
        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            supplierOrdersScrollPane.setVisible(newValue);
            supplierOrdersScrollPane.setManaged(newValue);
        });

        // Listen to selectedCount property
        selectedCount.addListener((obs, oldCount, newCount) -> {
            boolean isAnyRowSelected = newCount.intValue() > 0;
            tableToolbarController.toggleButtonVisibilityOnSelection(isAnyRowSelected);
        });

        // Listen to Dialog confirmations
        Consumer<List<SupplierOrder>> onConfirmDelete = this::handleDeleteOrders;
        Runnable onCancelDelete = this::closeConfirmDeleteDialog;
        confirmDialogDeleteListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);

        // Listen to the toolbar buttons
        tableToolbarController.getCancelRowSelectionButton().setOnAction(e -> cancelSelectionsAndEdit());
        tableToolbarController.getEditSelectedRowsButton().setOnAction(e -> editSelectedRows());
        tableToolbarController.getSaveChangesButton().setOnAction(e -> saveEditedRows());
        tableToolbarController.getDeleteSelectedRowsButton().setOnAction(e -> openConfirmDeleteDialog(selectedRowsIndices));;
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
                tableView.getItems().add(tableRow);
            }
        });

        return supplierOrdersOptional;
    }

    private Optional<PaginatedResults<SupplierOrder>> handleOrdersException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supplier orders."));
        return Optional.empty();
    }

    private void setRowListeners(TableData<SupplierOrder> supplierOrder) {
        // Add listener to the selectedProperty
        supplierOrder.isSelectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (Boolean.TRUE.equals(isSelected)) {
                selectedRowsIndices.add(tableView.getItems().indexOf(supplierOrder));
            } else {
                selectedRowsIndices.remove(Integer.valueOf(tableView.getItems().indexOf(supplierOrder)));
            }
            selectedCount.set(selectedRowsIndices.size());
        });
    }

    private void editSelectedRows() {
        tableToolbarController.toggleButtonVisibilityOnEdit(true);
        isEditMode.set(true);
        tableView.refresh();
    }

    private void cancelSelectionsAndEdit() {
        isEditMode.set(false);
        tableView.getSelectionModel().clearSelection();

        tableToolbarController.toggleButtonVisibilityOnCancel();

        // Deselect all rows and clear recording array
        List<Integer> indicesToClear = new ArrayList<>(selectedRowsIndices);
        for (Integer rowIndex : indicesToClear) {
            tableView.getItems().get(rowIndex).setSelected(false);
        }
        selectedRowsIndices.clear();

        tableView.refresh();
    }

    private void saveEditedRows() {

    }

    private void loadConfirmDeleteDialog() {
        //Load delete dialog
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/confirmdialog/GenericConfirmDialogView.fxml",
                controllerFactory::createController);

        try {
            Node confirmDialogView = loader.load();
            confirmSupplierOrderDeleteController = loader.getController();
            confirmSupplierOrderDeleteController.setActionListener(confirmDialogDeleteListener);
            confirmDeleteDialogContainer.getChildren().add(confirmDialogView);
            closeConfirmDeleteDialog();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    // Utils
    private void openConfirmDeleteDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Supplier Orders Delete",
                "Are you sure you want to delete selected orders?",
                null);
        List<SupplierOrder> selectedOrders = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedOrders.add(tableView.getItems().get(index).getData());
        }
        confirmSupplierOrderDeleteController.setData(selectedOrders, confirmDialogInput);
        confirmDeleteDialogContainer.setVisible(true);
        confirmDeleteDialogContainer.setManaged(true);
    }

    private void closeConfirmDeleteDialog() {
        confirmDeleteDialogContainer.setVisible(false);
        confirmDeleteDialogContainer.setManaged(false);
    }

    private void handleDeleteOrders(List<SupplierOrder> supplierOrders) {
        List<Integer> ordersToRemoveIds = new ArrayList<>();
        for (SupplierOrder order : supplierOrders) {
            ordersToRemoveIds.add(order.getId());
        }
        supplierOrdersService.deleteSupplierOrderInBulk(ordersToRemoveIds)
                .thenAccept(result -> System.out.println("Orders deleted successfully:" + ordersToRemoveIds));


        tableView.getItems().removeIf(tableData -> ordersToRemoveIds.contains(tableData.getData().getId()));

        closeConfirmDeleteDialog();
        isEditMode.set(false);
        tableView.refresh();
    }



}

