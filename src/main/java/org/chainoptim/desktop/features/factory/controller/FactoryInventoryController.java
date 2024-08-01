package org.chainoptim.desktop.features.factory.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.model.FactoryInventoryItem;
import org.chainoptim.desktop.features.factory.service.FactoryInventoryItemService;
import org.chainoptim.desktop.features.factory.service.FactoryInventoryItemWriteService;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.productpipeline.model.Component;
import org.chainoptim.desktop.features.factory.dto.CreateFactoryInventoryItemDTO;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryInventoryItemDTO;
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

public class FactoryInventoryController implements DataReceiver<SearchData<Factory>> {

    // Services
    private final FactoryInventoryItemService factoryInventoryItemService;
    private final FactoryInventoryItemWriteService factoryInventoryItemWriteService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;

    // Controllers
    private TableToolbarController tableToolbarController;
    private PageSelectorController pageSelectorController;
    private final SelectComponentLoader selectComponentLoader;
    private final SelectProductLoader selectProductLoader;
    private GenericConfirmDialogController<List<FactoryInventoryItem>> confirmFactoryInventoryItemUpdateController;
    private GenericConfirmDialogController<List<FactoryInventoryItem>> confirmFactoryInventoryItemDeleteController;
    private GenericConfirmDialogController<List<FactoryInventoryItem>> confirmFactoryInventoryItemCreateController;

    // State
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;
    private SearchMode searchMode;
    private Factory factory;
    private long totalRowsCount;
    private int newInventoryItemCount = 0;
    private final List<Integer> selectedRowsIndices = new ArrayList<>();
    private final SimpleIntegerProperty selectedCount = new SimpleIntegerProperty(0);
    private final BooleanProperty isEditMode = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isNewOrderMode = new SimpleBooleanProperty(false);

    // Confirm Dialog Listeners
    private RunnableConfirmDialogActionListener<List<FactoryInventoryItem>> confirmDialogUpdateListener;
    private RunnableConfirmDialogActionListener<List<FactoryInventoryItem>> confirmDialogDeleteListener;
    private RunnableConfirmDialogActionListener<List<FactoryInventoryItem>> confirmDialogCreateListener;

    // FXML
    @FXML
    private StackPane tableToolbarContainer;
    @FXML
    private ScrollPane factoryInventoryScrollPane;
    @FXML
    private TableView<TableData<FactoryInventoryItem>> tableView;
    @FXML
    private TableColumn<TableData<FactoryInventoryItem>, Boolean> selectRowColumn;
    @FXML
    private TableColumn<TableData<FactoryInventoryItem>, Integer> inventoryIdColumn;
    @FXML
    private TableColumn<TableData<FactoryInventoryItem>, String> companyIdColumn;
    @FXML
    private TableColumn<TableData<FactoryInventoryItem>, String> factoryNameColumn;
    @FXML
    private TableColumn<TableData<FactoryInventoryItem>, String> componentNameColumn;
    @FXML
    private TableColumn<TableData<FactoryInventoryItem>, String> productNameColumn;
    @FXML
    private TableColumn<TableData<FactoryInventoryItem>, Float> quantityColumn;
    @FXML
    private TableColumn<TableData<FactoryInventoryItem>, Float> minimumRequiredQuantityColumn;
    @FXML
    private TableColumn<TableData<FactoryInventoryItem>, LocalDateTime> createdAtColumn;
    @FXML
    private TableColumn<TableData<FactoryInventoryItem>, LocalDateTime> updatedAtColumn;
    @FXML
    private StackPane pageSelectorContainer;
    @FXML
    private StackPane confirmUpdateDialogContainer;
    @FXML
    private StackPane confirmDeleteDialogContainer;
    @FXML
    private StackPane confirmCreateDialogContainer;


    @Inject
    public FactoryInventoryController(FactoryInventoryItemService factoryInventoryItemService,
                                    FactoryInventoryItemWriteService factoryInventoryItemWriteService,
                                    CommonViewsLoader commonViewsLoader,
                                    SelectComponentLoader selectComponentLoader,
                                    SelectProductLoader selectProductLoader,
                                    ToastManager toastManager,
                                    FallbackManager fallbackManager,
                                    SearchParams searchParams) {
        this.factoryInventoryItemService = factoryInventoryItemService;
        this.factoryInventoryItemWriteService = factoryInventoryItemWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.selectComponentLoader = selectComponentLoader;
        this.selectProductLoader = selectProductLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void setData(SearchData<Factory> searchData) {
        this.factory = searchData.getData();
        this.searchMode = searchData.getSearchMode();

        searchParams.setItemsPerPage(20);
        SearchOptions searchOptions = SearchOptionsConfiguration.getSearchOptions(Feature.FACTORY_INVENTORY);
        if (searchOptions == null) {
            throw new IllegalArgumentException("Search options not found");
        }

        tableToolbarController = commonViewsLoader.initializeTableToolbar(tableToolbarContainer);
        tableToolbarController.initialize(new ListHeaderParams(
                searchMode, searchParams,
                "Factory Inventory", "/img/box-solid.png", Feature.FACTORY_INVENTORY,
                searchOptions.getSortOptions(), searchOptions.getFilterOptions(),
                () -> loadFactoryInventoryItems(searchMode == SearchMode.SECONDARY ? factory.getId() : null, searchMode), null, null));
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
        selectComponentLoader.initialize();
        selectProductLoader.initialize();

        TableConfigurer.configureTableView(tableView, selectRowColumn);
        configureTableColumns();
        setUpListeners();
        loadConfirmDialogs();

        loadFactoryInventoryItems(searchMode == SearchMode.SECONDARY ? factory.getId() : null, searchMode);
    }

    // Loading
    private void loadConfirmDialogs() {
        confirmFactoryInventoryItemCreateController = commonViewsLoader.loadConfirmDialog(confirmCreateDialogContainer);
        confirmFactoryInventoryItemCreateController.setActionListener(confirmDialogCreateListener);
        closeConfirmCreateDialog();

        confirmFactoryInventoryItemUpdateController = commonViewsLoader.loadConfirmDialog(confirmUpdateDialogContainer);
        confirmFactoryInventoryItemUpdateController.setActionListener(confirmDialogUpdateListener);
        closeConfirmUpdateDialog();

        confirmFactoryInventoryItemDeleteController = commonViewsLoader.loadConfirmDialog(confirmDeleteDialogContainer);
        confirmFactoryInventoryItemDeleteController.setActionListener(confirmDialogDeleteListener);
        closeConfirmDeleteDialog();
    }

    // Configuration
    // - Table columns
    private void configureTableColumns() {
        // Bind columns to data
        selectRowColumn.setCellValueFactory(data -> data.getValue().isSelectedProperty());
        inventoryIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getId()));
        companyIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getCompanyId() != null ? data.getValue().getData().getCompanyId() : "N/A"));
        factoryNameColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(this.factory.getName()));
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
        companyIdColumn.setCellFactory(column -> new EditableCell<TableData<FactoryInventoryItem>, String>(
                isEditMode, selectedRowsIndices, String::toString) {
            @Override
            protected void commitChange(TableData<FactoryInventoryItem> item, String newValue) {
                item.getData().setCompanyId(newValue);
            }
        });
        quantityColumn.setCellFactory(column -> new EditableCell<TableData<FactoryInventoryItem>, Float>(
                isEditMode, selectedRowsIndices, Float::parseFloat) {
            @Override
            protected void commitChange(TableData<FactoryInventoryItem> item, Float newValue) {
                item.getData().setQuantity(newValue);
            }
        });
        minimumRequiredQuantityColumn.setCellFactory(column -> new EditableCell<TableData<FactoryInventoryItem>, Float>(
                isEditMode, selectedRowsIndices, Float::parseFloat) {
            @Override
            protected void commitChange(TableData<FactoryInventoryItem> item, Float newValue) {
                item.getData().setMinimumRequiredQuantity(newValue);
            }
        });
        createdAtColumn.setCellFactory(column -> new DateTimePickerCell<TableData<FactoryInventoryItem>, LocalDateTime>(
                isEditMode, selectedRowsIndices, false){});

        updatedAtColumn.setCellFactory(column -> new DateTimePickerCell<TableData<FactoryInventoryItem>, LocalDateTime>(
                isEditMode, selectedRowsIndices, false){});

        componentNameColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<FactoryInventoryItem>, String>(
                isEditMode, selectedRowsIndices, null, selectComponentLoader.getComponentsName()) {
            @Override
            protected void commitChange(TableData<FactoryInventoryItem> item, String newValue) {
                Component component = new Component();
                component.setId(selectComponentLoader.getComponentIdByName(newValue));
                component.setName(newValue);
                item.getData().setComponent(component);
            }
        });
        productNameColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<FactoryInventoryItem>, String>(
                isEditMode, selectedRowsIndices, null, selectProductLoader.getProductsName()) {
            @Override
            protected void commitChange(TableData<FactoryInventoryItem> item, String newValue) {
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
        searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadFactoryInventoryItems(factory.getId(), searchMode));
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadFactoryInventoryItems(factory.getId(), searchMode));
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadFactoryInventoryItems(factory.getId(), searchMode));
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadFactoryInventoryItems(factory.getId(), searchMode));
        searchParams.getFiltersProperty().addListener((MapChangeListener.Change<? extends String, ? extends String> change) -> {
            if (searchParams.getFiltersProperty().entrySet().size() == 1) { // Allow only one filter at a time
                loadFactoryInventoryItems(factory.getId(), searchMode);
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
        tableToolbarController.getCreateNewShipmentButton().setOnAction(e -> addNewOrder());
    }

    private void setUpConfirmDialogListeners() {
        Consumer<List<FactoryInventoryItem>> onConfirmDelete = this::handleDeleteOrders;
        Runnable onCancelDelete = this::closeConfirmDeleteDialog;
        confirmDialogDeleteListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);

        Consumer<List<FactoryInventoryItem>> onConfirmUpdate = this::handleUpdateOrders;
        Runnable onCancelUpdate = this::closeConfirmUpdateDialog;
        confirmDialogUpdateListener = new RunnableConfirmDialogActionListener<>(onConfirmUpdate, onCancelUpdate);

        Consumer<List<FactoryInventoryItem>> onConfirmCreate = this::handleCreateOrders;
        Runnable onCancelCreate = this::closeConfirmCreateDialog;
        confirmDialogCreateListener = new RunnableConfirmDialogActionListener<>(onConfirmCreate, onCancelCreate);
    }

    private void setUpRowListeners(TableData<FactoryInventoryItem> factoryInventoryItem) {
        // Add listener to the selectedProperty
        factoryInventoryItem.isSelectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (Boolean.TRUE.equals(isSelected)) {
                selectedRowsIndices.add(tableView.getItems().indexOf(factoryInventoryItem));
            } else {
                selectedRowsIndices.remove(Integer.valueOf(tableView.getItems().indexOf(factoryInventoryItem)));
            }
            selectedCount.set(selectedRowsIndices.size());
        });
    }

    // Data loading
    private void loadFactoryInventoryItems(Integer factoryId, SearchMode searchMode) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            fallbackManager.setLoading(false);
            return;
        }

        if (searchMode == SearchMode.SECONDARY) {
            if (factoryId == null) return;
            factoryInventoryItemService.getFactoryInventoryItemsByFactoryIdAdvanced(factoryId, searchParams, searchMode)
                    .thenApply(this::handleOrdersResponse)
                    .exceptionally(this::handleOrdersException);
        } else {
            if (currentUser.getOrganization().getId() == null) return;
            factoryInventoryItemService.getFactoryInventoryItemsByFactoryIdAdvanced(currentUser.getOrganization().getId(), searchParams, searchMode)
                    .thenApply(this::handleOrdersResponse)
                    .exceptionally(this::handleOrdersException);
        }
    }

    private Result<PaginatedResults<FactoryInventoryItem>> handleOrdersResponse(Result<PaginatedResults<FactoryInventoryItem>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("No items found");
                return;
            }
            PaginatedResults<FactoryInventoryItem> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalRowsCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalRowsCount);

            tableView.getItems().clear();
            if (paginatedResults.results.isEmpty()) {
                fallbackManager.setNoResults(true);
                return;
            }

            for (FactoryInventoryItem factoryInventoryItem : paginatedResults.results) {
                FactoryInventoryItem oldData = new FactoryInventoryItem(factoryInventoryItem);
                TableData<FactoryInventoryItem> tableRow = new TableData<>(factoryInventoryItem, oldData, new SimpleBooleanProperty(false));
                setUpRowListeners(tableRow);
                tableView.getItems().add(tableRow);
            }
        });

        return result;
    }

    private Result<PaginatedResults<FactoryInventoryItem>> handleOrdersException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load factory items."));
        return new Result<>();
    }

    // UI Actions
    private void addNewOrder() {
        isNewOrderMode.set(true);
        tableToolbarController.toggleButtonVisibilityOnCreate(isNewOrderMode.get());

        FactoryInventoryItem newOrder = new FactoryInventoryItem();
        TableData<FactoryInventoryItem> newOrderRow = new TableData<>(newOrder, newOrder, new SimpleBooleanProperty(false));
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
            TableData<FactoryInventoryItem> tableRow = tableView.getItems().get(index);
            FactoryInventoryItem oldOrder = new FactoryInventoryItem(tableRow.getData());
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
            TableData<FactoryInventoryItem> tableRow = tableView.getItems().get(rowIndex);
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
                "Confirm Factory Orders Create",
                "Are you sure you want to create new items?",
                null);
        List<FactoryInventoryItem> selectedItems = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedItems.add(tableView.getItems().get(index).getData());
        }
        confirmFactoryInventoryItemCreateController.setData(selectedItems, confirmDialogInput);
        toggleDialogVisibility(confirmCreateDialogContainer, true);
    }

    private void closeConfirmCreateDialog() {
        toggleDialogVisibility(confirmCreateDialogContainer, false);
    }

    private void openConfirmUpdateDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Factory Orders Update",
                "Are you sure you want to update selected items?",
                null);
        List<FactoryInventoryItem> selectedItems = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedItems.add(tableView.getItems().get(index).getData());
        }
        confirmFactoryInventoryItemUpdateController.setData(selectedItems, confirmDialogInput);
        toggleDialogVisibility(confirmUpdateDialogContainer, true);
    }

    private void closeConfirmUpdateDialog() {
        toggleDialogVisibility(confirmUpdateDialogContainer, false);
    }

    private void openConfirmDeleteDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Factory Orders Delete",
                "Are you sure you want to delete selected items?",
                null);
        List<FactoryInventoryItem> selectedItems = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedItems.add(tableView.getItems().get(index).getData());
        }
        confirmFactoryInventoryItemDeleteController.setData(selectedItems, confirmDialogInput);
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
    private void handleCreateOrders(List<FactoryInventoryItem> FactoryInventoryItems) {
        List<CreateFactoryInventoryItemDTO> createFactoryInventoryItemDTOs = new ArrayList<>();
        for (FactoryInventoryItem item : FactoryInventoryItems) {
            CreateFactoryInventoryItemDTO createFactoryInventoryItemDTO = getCreateFactoryInventoryItemDTO(item);

            createFactoryInventoryItemDTOs.add(createFactoryInventoryItemDTO);
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryInventoryItemWriteService.createFactoryInventoryItemsInBulk(createFactoryInventoryItemDTOs)
                .thenApply(this::handleCreateFactoryInventoryItemsResponse)
                .exceptionally(this::handleCreateFactoryInventoryItemsException);
    }

    private CreateFactoryInventoryItemDTO getCreateFactoryInventoryItemDTO(FactoryInventoryItem item) {
        CreateFactoryInventoryItemDTO createFactoryInventoryItemDTO = new CreateFactoryInventoryItemDTO();
        if (factory == null || factory.getOrganizationId() == null) {
            throw new IllegalArgumentException("Factory Organization ID is missing");
        }
        createFactoryInventoryItemDTO.setOrganizationId(factory.getOrganizationId());
        createFactoryInventoryItemDTO.setFactoryId(factory.getId());
        createFactoryInventoryItemDTO.setProductId(item.getProduct().getId());
        createFactoryInventoryItemDTO.setComponentId(item.getComponent().getId());
        createFactoryInventoryItemDTO.setQuantity(item.getQuantity());
        createFactoryInventoryItemDTO.setMinimumRequiredQuantity(item.getMinimumRequiredQuantity());
        createFactoryInventoryItemDTO.setCompanyId(item.getCompanyId());

        return createFactoryInventoryItemDTO;
    }

    private Result<List<FactoryInventoryItem>> handleCreateFactoryInventoryItemsResponse(Result<List<FactoryInventoryItem>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the Factory Orders.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }
            isNewOrderMode.set(false);
            closeConfirmCreateDialog();
            updateUIOnSuccessfulOperation();
            ToastInfo toastInfo = new ToastInfo("Success", "Factory Orders created successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<FactoryInventoryItem>> handleCreateFactoryInventoryItemsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the Factory Orders.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void handleUpdateOrders(List<FactoryInventoryItem> FactoryInventoryItems) {
        List<UpdateFactoryInventoryItemDTO> updateFactoryInventoryItemDTOs = new ArrayList<>();

        for (FactoryInventoryItem item : FactoryInventoryItems) {
            UpdateFactoryInventoryItemDTO updateFactoryInventoryItemDTO = getUpdateFactoryInventoryItemDTO(item);

            updateFactoryInventoryItemDTOs.add(updateFactoryInventoryItemDTO);
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryInventoryItemWriteService.updateFactoryInventoryItemsInBulk(updateFactoryInventoryItemDTOs)
                .thenApply(this::handleUpdateFactoryInventoryItemsResponse)
                .exceptionally(this::handleUpdateFactoryInventoryItemsException);
    }

    private UpdateFactoryInventoryItemDTO getUpdateFactoryInventoryItemDTO(FactoryInventoryItem item) {
        UpdateFactoryInventoryItemDTO updateFactoryInventoryItemDTO = new UpdateFactoryInventoryItemDTO();
        updateFactoryInventoryItemDTO.setOrganizationId(factory.getOrganizationId());
        updateFactoryInventoryItemDTO.setId(item.getId());
        updateFactoryInventoryItemDTO.setComponentId(item.getComponent().getId());
        updateFactoryInventoryItemDTO.setProductId(item.getProduct().getId());
        updateFactoryInventoryItemDTO.setQuantity(item.getQuantity());
        updateFactoryInventoryItemDTO.setMinimumRequiredQuantity(item.getMinimumRequiredQuantity());
        updateFactoryInventoryItemDTO.setCompanyId(item.getCompanyId());

        return updateFactoryInventoryItemDTO;
    }

    private Result<List<FactoryInventoryItem>> handleUpdateFactoryInventoryItemsResponse(Result<List<FactoryInventoryItem>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);

            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error updating the Factory Orders.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            isNewOrderMode.set(false);
            closeConfirmUpdateDialog();
            updateUIOnSuccessfulOperation();
            ToastInfo toastInfo = new ToastInfo("Success", "Factory Orders updated successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<FactoryInventoryItem>> handleUpdateFactoryInventoryItemsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error updating the Factory Orders.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void handleDeleteOrders(List<FactoryInventoryItem> FactoryInventoryItems) {
        List<Integer> itemsToRemoveIds = new ArrayList<>();
        for (FactoryInventoryItem item : FactoryInventoryItems) {
            itemsToRemoveIds.add(item.getId());
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryInventoryItemWriteService.deleteFactoryInventoryItemsInBulk(itemsToRemoveIds)
                .thenApply(this::handleDeleteFactoryInventoryItemsResponse)
                .exceptionally(this::handleDeleteFactoryInventoryItemsException);
    }

    private Result<List<Integer>> handleDeleteFactoryInventoryItemsResponse(Result<List<Integer>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);

            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error deleting the Factory Orders.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            tableView.getItems().removeIf(tableData -> result.getData().contains(tableData.getData().getId()));

            closeConfirmDeleteDialog();
            isEditMode.set(false);
            tableView.refresh();
            selectedRowsIndices.clear();
            ToastInfo toastInfo = new ToastInfo("Success", "Factory Orders deleted successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<Integer>> handleDeleteFactoryInventoryItemsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error deleting the Factory Orders.", OperationOutcome.ERROR);
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
            TableData<FactoryInventoryItem> tableRow = tableView.getItems().get(rowIndex);
            tableRow.setSelected(false);
        }

        selectRowColumn.setEditable(true);
        selectedRowsIndices.clear();
        tableView.refresh();
    }
}
