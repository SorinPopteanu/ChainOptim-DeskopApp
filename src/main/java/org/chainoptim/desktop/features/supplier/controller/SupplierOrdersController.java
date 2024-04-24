package org.chainoptim.desktop.features.supplier.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.productpipeline.model.Component;
import org.chainoptim.desktop.features.supplier.dto.CreateSupplierOrderDTO;
import org.chainoptim.desktop.features.supplier.dto.UpdateSupplierOrderDTO;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
import org.chainoptim.desktop.features.supplier.service.SupplierOrdersService;
import org.chainoptim.desktop.features.supplier.service.SupplierOrdersWriteService;
import org.chainoptim.desktop.shared.common.uielements.UIItem;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.enums.FilterType;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.enums.OrderStatus;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.filters.FilterOption;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.table.TableToolbarController;
import org.chainoptim.desktop.shared.table.edit.cell.ComboBoxEditableCell;
import org.chainoptim.desktop.shared.table.edit.cell.DateTimePickerCell;
import org.chainoptim.desktop.shared.table.edit.cell.EditableCell;
import org.chainoptim.desktop.shared.table.model.TableData;
import org.chainoptim.desktop.shared.table.util.TableConfigurer;
import org.chainoptim.desktop.shared.table.util.SelectComponentLoader;

import org.chainoptim.desktop.shared.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

public class SupplierOrdersController implements DataReceiver<Supplier> {

    // Services
    private final SupplierOrdersService supplierOrdersService;
    private final SupplierOrdersWriteService supplierOrdersWriteService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;

    // Controllers
    private TableToolbarController tableToolbarController;
    private PageSelectorController pageSelectorController;
    private final SelectComponentLoader selectComponentLoader;
    private GenericConfirmDialogController<List<SupplierOrder>> confirmSupplierOrderUpdateController;
    private GenericConfirmDialogController<List<SupplierOrder>> confirmSupplierOrderDeleteController;
    private GenericConfirmDialogController<List<SupplierOrder>> confirmSupplierOrderCreateController;

    // State
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;
    private static final List<FilterOption> filterOptions = List.of(
            new FilterOption(
                    new UIItem("Order Date Start", "orderDateStart"),
                    new ArrayList<>(),
                    FilterType.DATE
            ),
            new FilterOption(
                    new UIItem("Quantity", "greaterThanQuantity"),
                    new ArrayList<>(),
                    FilterType.NUMBER
            ),
            new FilterOption(
                    new UIItem("Status", "status"),
                    List.of(new UIItem("Delivered", "DELIVERED"), new UIItem("Pending", "PENDING")),
                    FilterType.ENUM
            )
    );
    private final Map<String, String> sortOptions = Map.of(
            "orderDate", "Order Date",
            "estimatedDeliveryDate", "Estimated Delivery Date",
            "deliveryDate", "Delivery Date",
            "quantity", "Quantity"
    );
    private Supplier supplier;
    private final List<OrderStatus> statusOptions = Arrays.asList(OrderStatus.values());
    private long totalRowsCount;
    private int newOrderCount = 0;
    private final List<Integer> selectedRowsIndices = new ArrayList<>();
    private final SimpleIntegerProperty selectedCount = new SimpleIntegerProperty(0);
    private final BooleanProperty isEditMode = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isNewOrderMode = new SimpleBooleanProperty(false);

    // Confirm Dialog Listeners
    private RunnableConfirmDialogActionListener<List<SupplierOrder>> confirmDialogUpdateListener;
    private RunnableConfirmDialogActionListener<List<SupplierOrder>> confirmDialogDeleteListener;
    private RunnableConfirmDialogActionListener<List<SupplierOrder>> confirmDialogCreateListener;

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
    private TableColumn<TableData<SupplierOrder>, OrderStatus> statusColumn;
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
    @FXML
    private StackPane confirmCreateDialogContainer;


    @Inject
    public SupplierOrdersController(SupplierOrdersService supplierOrdersService,
                                    SupplierOrdersWriteService supplierOrdersWriteService,
                                    CommonViewsLoader commonViewsLoader,
                                    ToastManager toastManager,
                                    FallbackManager fallbackManager,
                                    SearchParams searchParams,
                                    SelectComponentLoader selectComponentLoader) {
        this.supplierOrdersService = supplierOrdersService;
        this.supplierOrdersWriteService = supplierOrdersWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
        this.selectComponentLoader = selectComponentLoader;
    }

    @Override
    public void setData(Supplier supplier) {
        this.supplier = supplier;

        searchParams.setItemsPerPage(20);

        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
        tableToolbarController = commonViewsLoader.initializeTableToolbar(tableToolbarContainer);
        tableToolbarController.initialize(searchParams, filterOptions, sortOptions, () -> loadSupplierOrders(supplier.getId()));
        selectComponentLoader.initialize();

        TableConfigurer.configureTableView(tableView, selectRowColumn);
        configureTableColumns();
        setUpListeners();
        loadConfirmDialogs();

        loadSupplierOrders(supplier.getId());
    }

    // Loading
    private void loadConfirmDialogs() {
        confirmSupplierOrderCreateController = commonViewsLoader.loadConfirmDialog(confirmCreateDialogContainer);
        confirmSupplierOrderCreateController.setActionListener(confirmDialogCreateListener);
        closeConfirmCreateDialog();

        confirmSupplierOrderUpdateController = commonViewsLoader.loadConfirmDialog(confirmUpdateDialogContainer);
        confirmSupplierOrderUpdateController.setActionListener(confirmDialogUpdateListener);
        closeConfirmUpdateDialog();

        confirmSupplierOrderDeleteController = commonViewsLoader.loadConfirmDialog(confirmDeleteDialogContainer);
        confirmSupplierOrderDeleteController.setActionListener(confirmDialogDeleteListener);
        closeConfirmDeleteDialog();
    }

    // Configuration
    // - Table columns
    private void configureTableColumns() {
        // Bind columns to data
        selectRowColumn.setCellValueFactory(data -> data.getValue().isSelectedProperty());
        orderIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getId()));
        companyIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getCompanyId() != null ? data.getValue().getData().getCompanyId() : "N/A"));
        supplierNameColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(this.supplier.getName()));
        componentNameColumn.setCellValueFactory(data -> {
            Component component = data.getValue().getData().getComponent();
            String componentName = component != null ? component.getName() : "N/A";
            return new SimpleObjectProperty<>(componentName);
        });
        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getQuantity()));
        statusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getStatus()));
        orderDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getOrderDate()));
        estimatedDeliveryDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getEstimatedDeliveryDate()));
        deliveryDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getDeliveryDate()));

        configureColumnCellFactories();
    }

    private void configureColumnCellFactories() {
        companyIdColumn.setCellFactory(column -> new EditableCell<TableData<SupplierOrder>, String>(
                isEditMode, selectedRowsIndices, String::toString) {
            @Override
            protected void commitChange(TableData<SupplierOrder> item, String newValue) {
                item.getData().setCompanyId(newValue);
            }
        });
        quantityColumn.setCellFactory(column -> new EditableCell<TableData<SupplierOrder>, Float>(
                isEditMode, selectedRowsIndices, Float::parseFloat) {
            @Override
            protected void commitChange(TableData<SupplierOrder> item, Float newValue) {
                item.getData().setQuantity(newValue);
            }
        });
        estimatedDeliveryDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<SupplierOrder>, LocalDateTime>(
                isEditMode, selectedRowsIndices, true) {
            @Override
            protected void commitChange(TableData<SupplierOrder> item, LocalDateTime newValue) {
                item.getData().setEstimatedDeliveryDate(newValue);
            }
        });

        deliveryDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<SupplierOrder>, LocalDateTime>(
                isEditMode, selectedRowsIndices, true) {
            @Override
            protected void commitChange(TableData<SupplierOrder> item, LocalDateTime newValue) {
                item.getData().setDeliveryDate(newValue);
            }
        });

        orderDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<SupplierOrder>, LocalDateTime>(
                isEditMode, selectedRowsIndices, false){});

        statusColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<SupplierOrder>, OrderStatus>(
                isEditMode, selectedRowsIndices, null, statusOptions) {
            @Override
            protected void commitChange(TableData<SupplierOrder> item, OrderStatus newValue) {
                item.getData().setStatus(newValue);
            }
        });
        componentNameColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<SupplierOrder>, String>(
                isEditMode, selectedRowsIndices, null, selectComponentLoader.getComponentsName()) {
            @Override
            protected void commitChange(TableData<SupplierOrder> item, String newValue) {
                Component component = new Component();
                component.setId(selectComponentLoader.getComponentIdByName(newValue));
                component.setName(newValue);
                item.getData().setComponent(component);
            }
        });
    }

    // - Listeners
    private void setUpListeners() {
        setUpSearchListeners();
        setUpTableToolbarListeners();
        setUpConfirmDialogListeners();
    }

    private void setUpSearchListeners() {
        searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadSupplierOrders(supplier.getId()));
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadSupplierOrders(supplier.getId()));
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadSupplierOrders(supplier.getId()));
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadSupplierOrders(supplier.getId()));
        searchParams.getFiltersProperty().addListener((MapChangeListener.Change<? extends String, ? extends String> change) -> {
            System.out.println("Filter changed: " + change.getKey() + " -> " + change.getValueAdded());
            loadSupplierOrders(supplier.getId());
        });
    }

    private void setUpTableToolbarListeners() {
        selectedCount.addListener((obs, oldCount, newCount) -> {
            boolean isAnyRowSelected = newCount.intValue() > 0;
            tableToolbarController.toggleButtonVisibilityOnSelection(isAnyRowSelected);
        });

        // Listen to the toolbar buttons
        tableToolbarController.getCancelRowSelectionButton().setOnAction(e -> cancelSelectionsAndEdit());
        tableToolbarController.getEditSelectedRowsButton().setOnAction(e -> editSelectedRows());
        tableToolbarController.getSaveChangesButton().setOnAction(e -> {
            if (isNewOrderMode.get()) {
                openConfirmCreateDialog();
            } else {
                openConfirmUpdateDialog(selectedRowsIndices);
            }
        });
        tableToolbarController.getDeleteSelectedRowsButton().setOnAction(e -> openConfirmDeleteDialog(selectedRowsIndices));;
        tableToolbarController.getCreateNewOrderButton().setOnAction(e -> addNewOrder());
    }

    private void setUpConfirmDialogListeners() {
        Consumer<List<SupplierOrder>> onConfirmDelete = this::handleDeleteOrders;
        Runnable onCancelDelete = this::closeConfirmDeleteDialog;
        confirmDialogDeleteListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);

        Consumer<List<SupplierOrder>> onConfirmUpdate = this::handleUpdateOrders;
        Runnable onCancelUpdate = this::closeConfirmUpdateDialog;
        confirmDialogUpdateListener = new RunnableConfirmDialogActionListener<>(onConfirmUpdate, onCancelUpdate);

        Consumer<List<SupplierOrder>> onConfirmCreate = this::handleCreateOrders;
        Runnable onCancelCreate = this::closeConfirmCreateDialog;
        confirmDialogCreateListener = new RunnableConfirmDialogActionListener<>(onConfirmCreate, onCancelCreate);
    }

    private void setUpRowListeners(TableData<SupplierOrder> supplierOrder) {
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

    // Data loading
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
                SupplierOrder oldData = new SupplierOrder(supplierOrder);
                TableData<SupplierOrder> tableRow = new TableData<>(supplierOrder, oldData, new SimpleBooleanProperty(false));
                setUpRowListeners(tableRow);
                tableView.getItems().add(tableRow);
            }
        });

        return supplierOrdersOptional;
    }

    private Optional<PaginatedResults<SupplierOrder>> handleOrdersException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supplier orders."));
        return Optional.empty();
    }

    // UI Actions
    private void addNewOrder() {
        isNewOrderMode.set(true);
        tableToolbarController.toggleButtonVisibilityOnCreate(isNewOrderMode.get());

        SupplierOrder newOrder = new SupplierOrder();
        TableData<SupplierOrder> newOrderRow = new TableData<>(newOrder, newOrder, new SimpleBooleanProperty(false));
        tableView.getItems().addFirst(newOrderRow);
        newOrderRow.setSelected(true);

        selectedRowsIndices.clear();
        for (int i = 0; i <= newOrderCount; i++) {
            selectedRowsIndices.add(i);
        }
        newOrderCount++;
        isEditMode.set(true);
        selectRowColumn.setEditable(false);
        tableView.refresh();
    }

    private void editSelectedRows() {
        tableToolbarController.toggleButtonVisibilityOnEdit(true);
        isEditMode.set(true);
        for (Integer index : selectedRowsIndices) {
            TableData<SupplierOrder> tableRow = tableView.getItems().get(index);
            SupplierOrder oldOrder = new SupplierOrder(tableRow.getData());
            oldOrder.setComponent(new Component(tableRow.getData().getComponent()));
            tableRow.setOldData(oldOrder);
        }
        selectRowColumn.setEditable(false);
        tableView.refresh();
    }

    private void cancelSelectionsAndEdit() {
        isEditMode.set(false);
        tableView.getSelectionModel().clearSelection();

        tableToolbarController.toggleButtonVisibilityOnCancel();

        // Deselect all rows and clear recording array
        List<Integer> indicesToClear = new ArrayList<>(selectedRowsIndices);
        for (Integer rowIndex : indicesToClear) {
            TableData<SupplierOrder> tableRow = tableView.getItems().get(rowIndex);
            tableRow.setData(tableRow.getOldData());
            tableRow.setSelected(false);
        }
        selectedRowsIndices.clear();

        // Delete created new orders
        if (isNewOrderMode.get()) {
            for (int i = 0; i < newOrderCount; i++) {
                tableView.getItems().removeFirst();
            }
            isNewOrderMode.set(false);
            newOrderCount = 0;
        }
        selectRowColumn.setEditable(true);
        tableView.refresh();
    }

    // Confirm Dialogs
    private void openConfirmCreateDialog() {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Supplier Orders Create",
                "Are you sure you want to create new orders?",
                null);
        List<SupplierOrder> selectedOrders = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedOrders.add(tableView.getItems().get(index).getData());
        }
        confirmSupplierOrderCreateController.setData(selectedOrders, confirmDialogInput);
        toggleDialogVisibility(confirmCreateDialogContainer, true);
    }

    private void closeConfirmCreateDialog() {
        toggleDialogVisibility(confirmCreateDialogContainer, false);
    }

    private void openConfirmUpdateDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Supplier Orders Update",
                "Are you sure you want to update selected orders?",
                null);
        List<SupplierOrder> selectedOrders = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedOrders.add(tableView.getItems().get(index).getData());
        }
        confirmSupplierOrderUpdateController.setData(selectedOrders, confirmDialogInput);
        toggleDialogVisibility(confirmUpdateDialogContainer, true);
    }

    private void closeConfirmUpdateDialog() {
        toggleDialogVisibility(confirmUpdateDialogContainer, false);
    }

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
        toggleDialogVisibility(confirmDeleteDialogContainer, true);
    }

    private void closeConfirmDeleteDialog() {
        toggleDialogVisibility(confirmDeleteDialogContainer, false);
    }

    private void toggleDialogVisibility(StackPane dialogContainer, boolean isVisible) {
        dialogContainer.setVisible(isVisible);
        dialogContainer.setManaged(isVisible);
    }

    // Backend calls
    private void handleCreateOrders(List<SupplierOrder> supplierOrders) {
        try {
            List<CreateSupplierOrderDTO> createSupplierOrderDTOs = new ArrayList<>();

            for (SupplierOrder order : supplierOrders) {
                CreateSupplierOrderDTO createSupplierOrderDTO = getCreateSupplierOrderDTO(order);

                createSupplierOrderDTOs.add(createSupplierOrderDTO);
            }

            supplierOrdersWriteService.createSupplierOrdersInBulk(createSupplierOrderDTOs)
                    .thenAccept(createdOrders -> {
                        if (createdOrders != null) {
                            System.out.println("Orders created successfully: " + createdOrders);
                        } else {
                            System.out.println("Error creating orders");
                        }
                    });
            isNewOrderMode.set(false);
            closeConfirmCreateDialog();
            updateUIOnSuccessfulOperation();
            addToast();
        } catch (Exception e) {
            addToastError();
        }
    }

    private CreateSupplierOrderDTO getCreateSupplierOrderDTO(SupplierOrder order) {
        CreateSupplierOrderDTO createSupplierOrderDTO = new CreateSupplierOrderDTO();
        if (supplier == null || supplier.getOrganizationId() == null) {
            throw new IllegalArgumentException("Supplier Organization ID is missing");
        }
        createSupplierOrderDTO.setOrganizationId(supplier.getOrganizationId());
        createSupplierOrderDTO.setSupplierId(supplier.getId());
        createSupplierOrderDTO.setComponentId(order.getComponent().getId());
        createSupplierOrderDTO.setQuantity(order.getQuantity());
        createSupplierOrderDTO.setOrderDate(order.getOrderDate());
        createSupplierOrderDTO.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        createSupplierOrderDTO.setDeliveryDate(order.getDeliveryDate());
        createSupplierOrderDTO.setStatus(order.getStatus());
        createSupplierOrderDTO.setCompanyId(order.getCompanyId());

        return createSupplierOrderDTO;
    }

    private void handleUpdateOrders(List<SupplierOrder> supplierOrders) {
        List<UpdateSupplierOrderDTO> updateSupplierOrderDTOs = new ArrayList<>();

        for (SupplierOrder order : supplierOrders) {
            UpdateSupplierOrderDTO updateSupplierOrderDTO = getUpdateSupplierOrderDTO(order);

            updateSupplierOrderDTOs.add(updateSupplierOrderDTO);
        }

        supplierOrdersWriteService.updateSupplierOrdersInBulk(updateSupplierOrderDTOs)
                .thenAccept(updatedOrders -> {});
        closeConfirmUpdateDialog();
        updateUIOnSuccessfulOperation();
        addToastUpdate();
    }

    private UpdateSupplierOrderDTO getUpdateSupplierOrderDTO(SupplierOrder order) {
        UpdateSupplierOrderDTO updateSupplierOrderDTO = new UpdateSupplierOrderDTO();
        updateSupplierOrderDTO.setId(order.getId());
        updateSupplierOrderDTO.setOrganizationId(order.getOrganizationId());
        updateSupplierOrderDTO.setComponentId(order.getComponent().getId());
        updateSupplierOrderDTO.setQuantity(order.getQuantity());
        updateSupplierOrderDTO.setOrderDate(order.getOrderDate());
        updateSupplierOrderDTO.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        updateSupplierOrderDTO.setDeliveryDate(order.getDeliveryDate());
        updateSupplierOrderDTO.setStatus(order.getStatus());
        updateSupplierOrderDTO.setCompanyId(order.getCompanyId());

        return updateSupplierOrderDTO;
    }

    private void handleDeleteOrders(List<SupplierOrder> supplierOrders) {
        List<Integer> ordersToRemoveIds = new ArrayList<>();
        for (SupplierOrder order : supplierOrders) {
            ordersToRemoveIds.add(order.getId());
        }

        try {
            supplierOrdersWriteService.deleteSupplierOrderInBulk(ordersToRemoveIds)
                    .thenAccept(result -> System.out.println("Orders deleted successfully:" + ordersToRemoveIds));
        } catch (Exception e) {
            e.printStackTrace();
        }

        tableView.getItems().removeIf(tableData -> ordersToRemoveIds.contains(tableData.getData().getId()));

        closeConfirmDeleteDialog();
        isEditMode.set(false);
        tableView.refresh();
        selectedRowsIndices.clear();
    }

    private void updateUIOnSuccessfulOperation() {
        isEditMode.set(false);
        newOrderCount = 0;
        tableView.getSelectionModel().clearSelection();
        tableToolbarController.toggleButtonVisibilityOnCancel();

        List<Integer> indicesToClear = new ArrayList<>(selectedRowsIndices);
        for (Integer rowIndex : indicesToClear) {
            TableData<SupplierOrder> tableRow = tableView.getItems().get(rowIndex);
            tableRow.setSelected(false);
        }

        selectRowColumn.setEditable(true);
        selectedRowsIndices.clear();
        tableView.refresh();
    }

    // Toasts
    private void addToast() {
        ToastInfo toastInfo = new ToastInfo("Success", "The supplier order has been created successfully", OperationOutcome.SUCCESS);
        toastManager.addToast(toastInfo);
    }

    private void addToastUpdate() {
        ToastInfo toastInfo = new ToastInfo("Success", "The supplier order has been updated successfully", OperationOutcome.SUCCESS);
        toastManager.addToast(toastInfo);
    }

    private void addToastError() {
        ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the supplier order", OperationOutcome.ERROR);
        toastManager.addToast(toastInfo);
    }
}