package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.warehouse.model.WarehouseInventoryItem;
import org.chainoptim.desktop.features.warehouse.service.WarehouseInventoryItemService;
import org.chainoptim.desktop.features.warehouse.service.WarehouseInventoryItemWriteService;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.productpipeline.model.Component;
import org.chainoptim.desktop.features.warehouse.dto.CreateWarehouseInventoryItemDTO;
import org.chainoptim.desktop.features.warehouse.dto.UpdateWarehouseInventoryItemDTO;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.enums.OrderStatus;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.*;
import org.chainoptim.desktop.shared.table.TableToolbarController;
import org.chainoptim.desktop.shared.table.edit.cell.ComboBoxEditableCell;
import org.chainoptim.desktop.shared.table.edit.cell.DateTimePickerCell;
import org.chainoptim.desktop.shared.table.edit.cell.EditableCell;
import org.chainoptim.desktop.shared.table.model.TableData;
import org.chainoptim.desktop.shared.table.util.SelectProductLoader;
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

public class WarehouseInventoryController implements DataReceiver<Warehouse> {

    // Services
    private final WarehouseInventoryItemService warehouseInventoryItemService;
    private final WarehouseInventoryItemWriteService warehouseInventoryItemWriteService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;

    // Controllers
    private TableToolbarController tableToolbarController;
    private PageSelectorController pageSelectorController;
    private final SelectComponentLoader selectComponentLoader;
    private final SelectProductLoader selectProductLoader;
    private GenericConfirmDialogController<List<WarehouseInventoryItem>> confirmWarehouseInventoryItemUpdateController;
    private GenericConfirmDialogController<List<WarehouseInventoryItem>> confirmWarehouseInventoryItemDeleteController;
    private GenericConfirmDialogController<List<WarehouseInventoryItem>> confirmWarehouseInventoryItemCreateController;

    // State
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;

    private Warehouse warehouse;
    private final List<OrderStatus> statusOptions = Arrays.asList(OrderStatus.values());
    private long totalRowsCount;
    private int newInventoryItemCount = 0;
    private final List<Integer> selectedRowsIndices = new ArrayList<>();
    private final SimpleIntegerProperty selectedCount = new SimpleIntegerProperty(0);
    private final BooleanProperty isEditMode = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isNewOrderMode = new SimpleBooleanProperty(false);

    // Confirm Dialog Listeners
    private RunnableConfirmDialogActionListener<List<WarehouseInventoryItem>> confirmDialogUpdateListener;
    private RunnableConfirmDialogActionListener<List<WarehouseInventoryItem>> confirmDialogDeleteListener;
    private RunnableConfirmDialogActionListener<List<WarehouseInventoryItem>> confirmDialogCreateListener;

    // FXML
    @FXML
    private StackPane tableToolbarContainer;
    @FXML
    private ScrollPane warehouseInventoryScrollPane;
    @FXML
    private TableView<TableData<WarehouseInventoryItem>> tableView;
    @FXML
    private TableColumn<TableData<WarehouseInventoryItem>, Boolean> selectRowColumn;
    @FXML
    private TableColumn<TableData<WarehouseInventoryItem>, Integer> inventoryIdColumn;
    @FXML
    private TableColumn<TableData<WarehouseInventoryItem>, String> companyIdColumn;
    @FXML
    private TableColumn<TableData<WarehouseInventoryItem>, String> warehouseNameColumn;
    @FXML
    private TableColumn<TableData<WarehouseInventoryItem>, String> componentNameColumn;
    @FXML
    private TableColumn<TableData<WarehouseInventoryItem>, String> productNameColumn;
    @FXML
    private TableColumn<TableData<WarehouseInventoryItem>, Float> quantityColumn;
    @FXML
    private TableColumn<TableData<WarehouseInventoryItem>, Float> minimumRequiredQuantityColumn;
    @FXML
    private TableColumn<TableData<WarehouseInventoryItem>, LocalDateTime> createdAtColumn;
    @FXML
    private TableColumn<TableData<WarehouseInventoryItem>, LocalDateTime> updatedAtColumn;
    @FXML
    private StackPane pageSelectorContainer;
    @FXML
    private StackPane confirmUpdateDialogContainer;
    @FXML
    private StackPane confirmDeleteDialogContainer;
    @FXML
    private StackPane confirmCreateDialogContainer;


    @Inject
    public WarehouseInventoryController(WarehouseInventoryItemService warehouseInventoryItemService,
                                      WarehouseInventoryItemWriteService warehouseInventoryItemWriteService,
                                      CommonViewsLoader commonViewsLoader,
                                      SelectComponentLoader selectComponentLoader,
                                      SelectProductLoader selectProductLoader,
                                      ToastManager toastManager,
                                      FallbackManager fallbackManager,
                                      SearchParams searchParams) {
        this.warehouseInventoryItemService = warehouseInventoryItemService;
        this.warehouseInventoryItemWriteService = warehouseInventoryItemWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.selectComponentLoader = selectComponentLoader;
        this.selectProductLoader = selectProductLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void setData(Warehouse warehouse) {
        this.warehouse = warehouse;

        searchParams.setItemsPerPage(20);
        SearchOptions searchOptions = SearchOptionsConfiguration.getSearchOptions(Feature.WAREHOUSE_INVENTORY);
        if (searchOptions == null) {
            throw new IllegalArgumentException("Search options not found");
        }

        tableToolbarController = commonViewsLoader.initializeTableToolbar(tableToolbarContainer);
        tableToolbarController.initialize(new ListHeaderParams(
                SearchMode.SECONDARY, searchParams,
                "Factory Inventory", "/img/box-solid.png", Feature.WAREHOUSE_INVENTORY,
                searchOptions.getSortOptions(), searchOptions.getFilterOptions(),
                () -> loadWarehouseInventoryItems(warehouse.getId()), null, null));
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
        selectComponentLoader.initialize();
        selectProductLoader.initialize();

        TableConfigurer.configureTableView(tableView, selectRowColumn);
        configureTableColumns();
        setUpListeners();
        loadConfirmDialogs();

        loadWarehouseInventoryItems(warehouse.getId());
    }

    // Loading
    private void loadConfirmDialogs() {
        confirmWarehouseInventoryItemCreateController = commonViewsLoader.loadConfirmDialog(confirmCreateDialogContainer);
        confirmWarehouseInventoryItemCreateController.setActionListener(confirmDialogCreateListener);
        closeConfirmCreateDialog();

        confirmWarehouseInventoryItemUpdateController = commonViewsLoader.loadConfirmDialog(confirmUpdateDialogContainer);
        confirmWarehouseInventoryItemUpdateController.setActionListener(confirmDialogUpdateListener);
        closeConfirmUpdateDialog();

        confirmWarehouseInventoryItemDeleteController = commonViewsLoader.loadConfirmDialog(confirmDeleteDialogContainer);
        confirmWarehouseInventoryItemDeleteController.setActionListener(confirmDialogDeleteListener);
        closeConfirmDeleteDialog();
    }

    // Configuration
    // - Table columns
    private void configureTableColumns() {
        // Bind columns to data
        selectRowColumn.setCellValueFactory(data -> data.getValue().isSelectedProperty());
        inventoryIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getId()));
        companyIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getCompanyId() != null ? data.getValue().getData().getCompanyId() : "N/A"));
        warehouseNameColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(this.warehouse.getName()));
        componentNameColumn.setCellValueFactory(data -> {
            Component component = data.getValue().getData().getComponent();
            String componentName = component != null ? component.getName() : "N/A";
            return new SimpleObjectProperty<>(componentName);
        });
        productNameColumn.setCellValueFactory(data -> {
            Product product = data.getValue().getData().getProduct();
            String productName = product != null ? product.getName() : "N/A";
            return new SimpleObjectProperty<>(productName);
        });
        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getQuantity()));
        minimumRequiredQuantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getMinimumRequiredQuantity()));
        createdAtColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getCreatedAt()));
        updatedAtColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getUpdatedAt()));

        configureColumnCellFactories();
    }

    private void configureColumnCellFactories() {
        companyIdColumn.setCellFactory(column -> new EditableCell<TableData<WarehouseInventoryItem>, String>(
                isEditMode, selectedRowsIndices, String::toString) {
            @Override
            protected void commitChange(TableData<WarehouseInventoryItem> item, String newValue) {
                item.getData().setCompanyId(newValue);
            }
        });
        quantityColumn.setCellFactory(column -> new EditableCell<TableData<WarehouseInventoryItem>, Float>(
                isEditMode, selectedRowsIndices, Float::parseFloat) {
            @Override
            protected void commitChange(TableData<WarehouseInventoryItem> item, Float newValue) {
                item.getData().setQuantity(newValue);
            }
        });
        minimumRequiredQuantityColumn.setCellFactory(column -> new EditableCell<TableData<WarehouseInventoryItem>, Float>(
                isEditMode, selectedRowsIndices, Float::parseFloat) {
            @Override
            protected void commitChange(TableData<WarehouseInventoryItem> item, Float newValue) {
                item.getData().setMinimumRequiredQuantity(newValue);
                System.out.println("Minimum required quantity: " + item.getData().getMinimumRequiredQuantity());
            }
        });
        createdAtColumn.setCellFactory(column -> new DateTimePickerCell<TableData<WarehouseInventoryItem>, LocalDateTime>(
                isEditMode, selectedRowsIndices, false){});

        updatedAtColumn.setCellFactory(column -> new DateTimePickerCell<TableData<WarehouseInventoryItem>, LocalDateTime>(
                isEditMode, selectedRowsIndices, false){});

        componentNameColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<WarehouseInventoryItem>, String>(
                isEditMode, selectedRowsIndices, null, selectComponentLoader.getComponentsName()) {
            @Override
            protected void commitChange(TableData<WarehouseInventoryItem> item, String newValue) {
                Component component = new Component();
                component.setId(selectComponentLoader.getComponentIdByName(newValue));
                component.setName(newValue);
                item.getData().setComponent(component);
            }
        });
        productNameColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<WarehouseInventoryItem>, String>(
                isEditMode, selectedRowsIndices, null, selectProductLoader.getProductsName()) {
            @Override
            protected void commitChange(TableData<WarehouseInventoryItem> item, String newValue) {
                Product product = new Product();
                product.setId(selectProductLoader.getProductIdByName(newValue));
                product.setName(newValue);
                item.getData().setProduct(product);
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
        searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadWarehouseInventoryItems(warehouse.getId()));
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadWarehouseInventoryItems(warehouse.getId()));
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadWarehouseInventoryItems(warehouse.getId()));
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadWarehouseInventoryItems(warehouse.getId()));
        searchParams.getFiltersProperty().addListener((MapChangeListener.Change<? extends String, ? extends String> change) -> {
            if (searchParams.getFiltersProperty().entrySet().size() == 1) { // Allow only one filter at a time
                loadWarehouseInventoryItems(warehouse.getId());
            }
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
        Consumer<List<WarehouseInventoryItem>> onConfirmDelete = this::handleDeleteOrders;
        Runnable onCancelDelete = this::closeConfirmDeleteDialog;
        confirmDialogDeleteListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);

        Consumer<List<WarehouseInventoryItem>> onConfirmUpdate = this::handleUpdateOrders;
        Runnable onCancelUpdate = this::closeConfirmUpdateDialog;
        confirmDialogUpdateListener = new RunnableConfirmDialogActionListener<>(onConfirmUpdate, onCancelUpdate);

        Consumer<List<WarehouseInventoryItem>> onConfirmCreate = this::handleCreateOrders;
        Runnable onCancelCreate = this::closeConfirmCreateDialog;
        confirmDialogCreateListener = new RunnableConfirmDialogActionListener<>(onConfirmCreate, onCancelCreate);
    }

    private void setUpRowListeners(TableData<WarehouseInventoryItem> WarehouseInventoryItem) {
        // Add listener to the selectedProperty
        WarehouseInventoryItem.isSelectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (Boolean.TRUE.equals(isSelected)) {
                selectedRowsIndices.add(tableView.getItems().indexOf(WarehouseInventoryItem));
            } else {
                selectedRowsIndices.remove(Integer.valueOf(tableView.getItems().indexOf(WarehouseInventoryItem)));
            }
            selectedCount.set(selectedRowsIndices.size());
        });
    }

    // Data loading
    private void loadWarehouseInventoryItems(Integer warehouseId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            fallbackManager.setLoading(false);
            return;
        }

        warehouseInventoryItemService.getWarehouseInventoryItemsByWarehouseIdAdvanced(warehouseId, searchParams)
                .thenApply(this::handleOrdersResponse)
                .exceptionally(this::handleOrdersException);
    }

    private Result<PaginatedResults<WarehouseInventoryItem>> handleOrdersResponse(Result<PaginatedResults<WarehouseInventoryItem>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("No items found");
                return;
            }
            PaginatedResults<WarehouseInventoryItem> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalRowsCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalRowsCount);

            tableView.getItems().clear();
            if (paginatedResults.results.isEmpty()) {
                fallbackManager.setNoResults(true);
                return;
            }

            for (WarehouseInventoryItem warehouseInventoryItem : paginatedResults.results) {
                WarehouseInventoryItem oldData = new WarehouseInventoryItem(warehouseInventoryItem);
                TableData<WarehouseInventoryItem> tableRow = new TableData<>(warehouseInventoryItem, oldData, new SimpleBooleanProperty(false));
                setUpRowListeners(tableRow);
                tableView.getItems().add(tableRow);
            }
        });

        return result;
    }

    private Result<PaginatedResults<WarehouseInventoryItem>> handleOrdersException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load warehouse items."));
        return new Result<>();
    }

    // UI Actions
    private void addNewOrder() {
        isNewOrderMode.set(true);
        tableToolbarController.toggleButtonVisibilityOnCreate(isNewOrderMode.get());

        WarehouseInventoryItem newOrder = new WarehouseInventoryItem();
        TableData<WarehouseInventoryItem> newOrderRow = new TableData<>(newOrder, newOrder, new SimpleBooleanProperty(false));
        tableView.getItems().addFirst(newOrderRow);
        newOrderRow.setSelected(true);

        selectedRowsIndices.clear();
        for (int i = 0; i <= newInventoryItemCount; i++) {
            selectedRowsIndices.add(i);
        }
        newInventoryItemCount++;
        isEditMode.set(true);
        selectRowColumn.setEditable(false);
        tableView.refresh();
    }

    private void editSelectedRows() {
        tableToolbarController.toggleButtonVisibilityOnEdit(true);
        isEditMode.set(true);
        for (Integer index : selectedRowsIndices) {
            TableData<WarehouseInventoryItem> tableRow = tableView.getItems().get(index);
            WarehouseInventoryItem oldOrder = new WarehouseInventoryItem(tableRow.getData());
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
            TableData<WarehouseInventoryItem> tableRow = tableView.getItems().get(rowIndex);
            tableRow.setData(tableRow.getOldData());
            tableRow.setSelected(false);
        }
        selectedRowsIndices.clear();

        // Delete created new items
        if (isNewOrderMode.get()) {
            for (int i = 0; i < newInventoryItemCount; i++) {
                tableView.getItems().removeFirst();
            }
            isNewOrderMode.set(false);
            newInventoryItemCount = 0;
        }
        selectRowColumn.setEditable(true);
        tableView.refresh();
    }

    // Confirm Dialogs
    private void openConfirmCreateDialog() {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Warehouse Orders Create",
                "Are you sure you want to create new items?",
                null);
        List<WarehouseInventoryItem> selectedItems = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedItems.add(tableView.getItems().get(index).getData());
        }
        confirmWarehouseInventoryItemCreateController.setData(selectedItems, confirmDialogInput);
        toggleDialogVisibility(confirmCreateDialogContainer, true);
    }

    private void closeConfirmCreateDialog() {
        toggleDialogVisibility(confirmCreateDialogContainer, false);
    }

    private void openConfirmUpdateDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Warehouse Orders Update",
                "Are you sure you want to update selected items?",
                null);
        List<WarehouseInventoryItem> selectedItems = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedItems.add(tableView.getItems().get(index).getData());
        }
        confirmWarehouseInventoryItemUpdateController.setData(selectedItems, confirmDialogInput);
        toggleDialogVisibility(confirmUpdateDialogContainer, true);
    }

    private void closeConfirmUpdateDialog() {
        toggleDialogVisibility(confirmUpdateDialogContainer, false);
    }

    private void openConfirmDeleteDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Warehouse Orders Delete",
                "Are you sure you want to delete selected items?",
                null);
        List<WarehouseInventoryItem> selectedItems = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedItems.add(tableView.getItems().get(index).getData());
        }
        confirmWarehouseInventoryItemDeleteController.setData(selectedItems, confirmDialogInput);
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
    // - Create
    private void handleCreateOrders(List<WarehouseInventoryItem> WarehouseInventoryItems) {
        List<CreateWarehouseInventoryItemDTO> createWarehouseInventoryItemDTOs = new ArrayList<>();
        for (WarehouseInventoryItem item : WarehouseInventoryItems) {
            CreateWarehouseInventoryItemDTO createWarehouseInventoryItemDTO = getCreateWarehouseInventoryItemDTO(item);

            createWarehouseInventoryItemDTOs.add(createWarehouseInventoryItemDTO);
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        warehouseInventoryItemWriteService.createWarehouseInventoryItemsInBulk(createWarehouseInventoryItemDTOs)
                .thenApply(this::handleCreateWarehouseInventoryItemsResponse)
                .exceptionally(this::handleCreateWarehouseInventoryItemsException);
    }

    private CreateWarehouseInventoryItemDTO getCreateWarehouseInventoryItemDTO(WarehouseInventoryItem item) {
        CreateWarehouseInventoryItemDTO createWarehouseInventoryItemDTO = new CreateWarehouseInventoryItemDTO();
        if (warehouse == null || warehouse.getOrganizationId() == null) {
            throw new IllegalArgumentException("Warehouse Organization ID is missing");
        }
        createWarehouseInventoryItemDTO.setOrganizationId(warehouse.getOrganizationId());
        createWarehouseInventoryItemDTO.setWarehouseId(warehouse.getId());
        createWarehouseInventoryItemDTO.setProductId(item.getProduct().getId());
        createWarehouseInventoryItemDTO.setComponentId(item.getComponent().getId());
        createWarehouseInventoryItemDTO.setQuantity(item.getQuantity());
        createWarehouseInventoryItemDTO.setMinimumRequiredQuantity(item.getMinimumRequiredQuantity());
        createWarehouseInventoryItemDTO.setCompanyId(item.getCompanyId());

        return createWarehouseInventoryItemDTO;
    }

    private Result<List<WarehouseInventoryItem>> handleCreateWarehouseInventoryItemsResponse(Result<List<WarehouseInventoryItem>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the Warehouse Orders.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                System.out.println("Error creating warehouse orders: " + result.getError().getMessage());
                return;
            }
            isNewOrderMode.set(false);
            closeConfirmCreateDialog();
            updateUIOnSuccessfulOperation();
            ToastInfo toastInfo = new ToastInfo("Success", "Warehouse Orders created successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<WarehouseInventoryItem>> handleCreateWarehouseInventoryItemsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the Warehouse Orders.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void handleUpdateOrders(List<WarehouseInventoryItem> WarehouseInventoryItems) {
        List<UpdateWarehouseInventoryItemDTO> updateWarehouseInventoryItemDTOs = new ArrayList<>();

        for (WarehouseInventoryItem item : WarehouseInventoryItems) {
            UpdateWarehouseInventoryItemDTO updateWarehouseInventoryItemDTO = getUpdateWarehouseInventoryItemDTO(item);

            updateWarehouseInventoryItemDTOs.add(updateWarehouseInventoryItemDTO);
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        warehouseInventoryItemWriteService.updateWarehouseInventoryItemsInBulk(updateWarehouseInventoryItemDTOs)
                .thenApply(this::handleUpdateWarehouseInventoryItemsResponse)
                .exceptionally(this::handleUpdateWarehouseInventoryItemsException);
    }

    private UpdateWarehouseInventoryItemDTO getUpdateWarehouseInventoryItemDTO(WarehouseInventoryItem item) {
        UpdateWarehouseInventoryItemDTO updateWarehouseInventoryItemDTO = new UpdateWarehouseInventoryItemDTO();
        updateWarehouseInventoryItemDTO.setId(item.getId());
        updateWarehouseInventoryItemDTO.setComponentId(item.getComponent().getId());
        updateWarehouseInventoryItemDTO.setProductId(item.getProduct().getId());
        updateWarehouseInventoryItemDTO.setQuantity(item.getQuantity());
        updateWarehouseInventoryItemDTO.setMinimumRequiredQuantity(item.getMinimumRequiredQuantity());
        updateWarehouseInventoryItemDTO.setCompanyId(item.getCompanyId());

        System.out.println("Updated item minimumQuantity: " + updateWarehouseInventoryItemDTO.getMinimumRequiredQuantity());
        return updateWarehouseInventoryItemDTO;
    }

    private Result<List<WarehouseInventoryItem>> handleUpdateWarehouseInventoryItemsResponse(Result<List<WarehouseInventoryItem>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);

            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error updating the Warehouse Orders.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                System.out.println("Error updating warehouse orders: " + result.getError().getMessage());
                return;
            }

            isNewOrderMode.set(false);
            closeConfirmUpdateDialog();
            updateUIOnSuccessfulOperation();
            ToastInfo toastInfo = new ToastInfo("Success", "Warehouse Orders updated successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<WarehouseInventoryItem>> handleUpdateWarehouseInventoryItemsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error updating the Warehouse Orders.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void handleDeleteOrders(List<WarehouseInventoryItem> warehouseInventoryItems) {
        List<Integer> itemsToRemoveIds = new ArrayList<>();
        for (WarehouseInventoryItem item : warehouseInventoryItems) {
            itemsToRemoveIds.add(item.getId());
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        warehouseInventoryItemWriteService.deleteWarehouseInventoryItemsInBulk(itemsToRemoveIds)
                .thenApply(this::handleDeleteWarehouseInventoryItemsResponse)
                .exceptionally(this::handleDeleteWarehouseInventoryItemsException);
    }

    private Result<List<Integer>> handleDeleteWarehouseInventoryItemsResponse(Result<List<Integer>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);

            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error deleting the Warehouse Orders.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            tableView.getItems().removeIf(tableData -> result.getData().contains(tableData.getData().getId()));

            closeConfirmDeleteDialog();
            isEditMode.set(false);
            tableView.refresh();
            selectedRowsIndices.clear();
            ToastInfo toastInfo = new ToastInfo("Success", "Warehouse Orders deleted successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<Integer>> handleDeleteWarehouseInventoryItemsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error deleting the Warehouse Orders.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void updateUIOnSuccessfulOperation() {
        isEditMode.set(false);
        newInventoryItemCount = 0;
        tableView.getSelectionModel().clearSelection();
        tableToolbarController.toggleButtonVisibilityOnCancel();

        List<Integer> indicesToClear = new ArrayList<>(selectedRowsIndices);
        for (Integer rowIndex : indicesToClear) {
            TableData<WarehouseInventoryItem> tableRow = tableView.getItems().get(rowIndex);
            tableRow.setSelected(false);
        }

        selectRowColumn.setEditable(true);
        selectedRowsIndices.clear();
        tableView.refresh();
    }
}
