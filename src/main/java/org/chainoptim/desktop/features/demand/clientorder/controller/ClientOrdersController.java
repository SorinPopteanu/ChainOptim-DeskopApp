package org.chainoptim.desktop.features.demand.clientorder.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.goods.product.model.Product;
import org.chainoptim.desktop.features.demand.clientorder.dto.CreateClientOrderDTO;
import org.chainoptim.desktop.features.demand.clientorder.dto.UpdateClientOrderDTO;
import org.chainoptim.desktop.features.demand.client.model.Client;
import org.chainoptim.desktop.features.demand.clientorder.model.ClientOrder;
import org.chainoptim.desktop.features.demand.clientorder.service.ClientOrdersService;
import org.chainoptim.desktop.features.demand.clientorder.service.ClientOrdersWriteService;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.model.ConfirmDialogInput;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ClientOrdersController implements DataReceiver<SearchData<Client>> {

    // Services
    private final ClientOrdersService clientOrdersService;
    private final ClientOrdersWriteService clientOrdersWriteService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;

    // Controllers
    private TableToolbarController tableToolbarController;
    private PageSelectorController pageSelectorController;
    private final SelectProductLoader selectProductLoader;
    private GenericConfirmDialogController<List<ClientOrder>> confirmClientOrderUpdateController;
    private GenericConfirmDialogController<List<ClientOrder>> confirmClientOrderDeleteController;
    private GenericConfirmDialogController<List<ClientOrder>> confirmClientOrderCreateController;

    // State
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;

    private SearchMode searchMode;
    private Client client;
    private final List<OrderStatus> statusOptions = Arrays.asList(OrderStatus.values());
    private long totalRowsCount;
    private int newOrderCount = 0;
    private final List<Integer> selectedRowsIndices = new ArrayList<>();
    private final SimpleIntegerProperty selectedCount = new SimpleIntegerProperty(0);
    private final BooleanProperty isEditMode = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isNewOrderMode = new SimpleBooleanProperty(false);

    // Confirm Dialog Listeners
    private RunnableConfirmDialogActionListener<List<ClientOrder>> confirmDialogUpdateListener;
    private RunnableConfirmDialogActionListener<List<ClientOrder>> confirmDialogDeleteListener;
    private RunnableConfirmDialogActionListener<List<ClientOrder>> confirmDialogCreateListener;

    // FXML
    @FXML
    private StackPane tableToolbarContainer;
    @FXML
    private ScrollPane clientOrdersScrollPane;
    @FXML
    private TableView<TableData<ClientOrder>> tableView;
    @FXML
    private TableColumn<TableData<ClientOrder>, Boolean> selectRowColumn;
    @FXML
    private TableColumn<TableData<ClientOrder>, Integer> orderIdColumn;
    @FXML
    private TableColumn<TableData<ClientOrder>, String> companyIdColumn;
    @FXML
    private TableColumn<TableData<ClientOrder>, String> clientNameColumn;
    @FXML
    private TableColumn<TableData<ClientOrder>, String> productNameColumn;
    @FXML
    private TableColumn<TableData<ClientOrder>, Float> quantityColumn;
    @FXML
    private TableColumn<TableData<ClientOrder>, Float> deliveredQuantityColumn;
    @FXML
    private TableColumn<TableData<ClientOrder>, OrderStatus> statusColumn;
    @FXML
    private TableColumn<TableData<ClientOrder>, LocalDateTime> orderDateColumn;
    @FXML
    private TableColumn<TableData<ClientOrder>, LocalDateTime> estimatedDeliveryDateColumn;
    @FXML
    private TableColumn<TableData<ClientOrder>, LocalDateTime> deliveryDateColumn;
    @FXML
    private StackPane pageSelectorContainer;
    @FXML
    private StackPane confirmUpdateDialogContainer;
    @FXML
    private StackPane confirmDeleteDialogContainer;
    @FXML
    private StackPane confirmCreateDialogContainer;
    @FXML
    private StackPane fallbackContainer;


    @Inject
    public ClientOrdersController(ClientOrdersService clientOrdersService,
                                  ClientOrdersWriteService clientOrdersWriteService,
                                  CommonViewsLoader commonViewsLoader,
                                  SelectProductLoader selectProductLoader,
                                  ToastManager toastManager,
                                  FallbackManager fallbackManager,
                                  SearchParams searchParams) {
        this.clientOrdersService = clientOrdersService;
        this.clientOrdersWriteService = clientOrdersWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.selectProductLoader = selectProductLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void setData(SearchData<Client> searchData) {
        this.client = searchData.getData();
        this.searchMode = searchData.getSearchMode();

        loadComponents();

        TableConfigurer.configureTableView(tableView, selectRowColumn);
        configureTableColumns();
        setUpListeners();
        loadConfirmDialogs();

        loadClientOrders(searchMode == SearchMode.SECONDARY ? client.getId() : null);
    }

    // Loading
    private void loadComponents() {
        searchParams.setItemsPerPage(20);
        SearchOptions searchOptions = SearchOptionsConfiguration.getSearchOptions(Feature.CLIENT_ORDER);

        commonViewsLoader.loadFallbackManager(fallbackContainer);
        tableToolbarController = commonViewsLoader.initializeTableToolbar(tableToolbarContainer);
        tableToolbarController.initialize(new ListHeaderParams
                (searchMode, searchParams,
                        "Client Orders", "/img/box-solid.png", Feature.CLIENT_ORDER,
                        searchOptions.getSortOptions(), searchOptions.getFilterOptions(),
                        () -> loadClientOrders(searchMode == SearchMode.SECONDARY ? client.getId() : null), null, null));
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
        selectProductLoader.initialize();
    }

    private void loadConfirmDialogs() {
        confirmClientOrderCreateController = commonViewsLoader.loadConfirmDialog(confirmCreateDialogContainer);
        confirmClientOrderCreateController.setActionListener(confirmDialogCreateListener);
        closeConfirmCreateDialog();

        confirmClientOrderUpdateController = commonViewsLoader.loadConfirmDialog(confirmUpdateDialogContainer);
        confirmClientOrderUpdateController.setActionListener(confirmDialogUpdateListener);
        closeConfirmUpdateDialog();

        confirmClientOrderDeleteController = commonViewsLoader.loadConfirmDialog(confirmDeleteDialogContainer);
        confirmClientOrderDeleteController.setActionListener(confirmDialogDeleteListener);
        closeConfirmDeleteDialog();
    }

    // Configuration
    // - Table columns
    private void configureTableColumns() {
        // Bind columns to data
        selectRowColumn.setCellValueFactory(data -> data.getValue().isSelectedProperty());
        orderIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getId()));
        companyIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getCompanyId() != null ? data.getValue().getData().getCompanyId() : "N/A"));
        clientNameColumn.setCellValueFactory(data -> new SimpleObjectProperty<>("this.client.getName()"));
        productNameColumn.setCellValueFactory(data -> {
            Product product = data.getValue().getData().getProduct();
            String productName = product != null ? product.getName() : "N/A";
            return new SimpleObjectProperty<>(productName);
        });
        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getQuantity()));
        deliveredQuantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getDeliveredQuantity()));
        statusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getStatus()));
        orderDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getOrderDate()));
        estimatedDeliveryDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getEstimatedDeliveryDate()));
        deliveryDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getDeliveryDate()));

        configureColumnCellFactories();
    }

    private void configureColumnCellFactories() {
        companyIdColumn.setCellFactory(column -> new EditableCell<TableData<ClientOrder>, String>(
                isEditMode, selectedRowsIndices, String::toString) {
            @Override
            protected void commitChange(TableData<ClientOrder> item, String newValue) {
                item.getData().setCompanyId(newValue);
            }
        });
        quantityColumn.setCellFactory(column -> new EditableCell<TableData<ClientOrder>, Float>(
                isEditMode, selectedRowsIndices, Float::parseFloat) {
            @Override
            protected void commitChange(TableData<ClientOrder> item, Float newValue) {
                item.getData().setQuantity(newValue);
            }
        });
        deliveredQuantityColumn.setCellFactory(column -> new EditableCell<TableData<ClientOrder>, Float>(
                isEditMode, selectedRowsIndices, Float::parseFloat) {
            @Override
            protected void commitChange(TableData<ClientOrder> item, Float newValue) {
                item.getData().setDeliveredQuantity(newValue);
            }
        });
        estimatedDeliveryDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<ClientOrder>, LocalDateTime>(
                isEditMode, selectedRowsIndices, true) {
            @Override
            protected void commitChange(TableData<ClientOrder> item, LocalDateTime newValue) {
                item.getData().setEstimatedDeliveryDate(newValue);
            }
        });

        deliveryDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<ClientOrder>, LocalDateTime>(
                isEditMode, selectedRowsIndices, true) {
            @Override
            protected void commitChange(TableData<ClientOrder> item, LocalDateTime newValue) {
                item.getData().setDeliveryDate(newValue);
            }
        });
        orderDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<ClientOrder>, LocalDateTime>(
                isEditMode, selectedRowsIndices, false){});

        statusColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<ClientOrder>, OrderStatus>(
                isEditMode, selectedRowsIndices, null, statusOptions) {
            @Override
            protected void commitChange(TableData<ClientOrder> item, OrderStatus newValue) {
                item.getData().setStatus(newValue);
            }
        });
        productNameColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<ClientOrder>, String>(
                isEditMode, selectedRowsIndices, null, selectProductLoader.getProductsName()) {
            @Override
            protected void commitChange(TableData<ClientOrder> item, String newValue) {
                Product product = new Product();
                product.setId(selectProductLoader.getProductIdByName(newValue));
                product.setName(newValue);
                item.getData().setProduct(product);
            }
        });
    }

    // - Listeners
    private void setUpListeners() {
        setUpFallbackManagerListener();
        setUpSearchListeners();
        setUpTableToolbarListeners();
        setUpConfirmDialogListeners();
    }

    private void setUpFallbackManagerListener() {
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            clientOrdersScrollPane.setVisible(newValue);
            clientOrdersScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void setUpSearchListeners() {
        searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadClientOrders(searchMode == SearchMode.SECONDARY ? client.getId() : null));
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadClientOrders(searchMode == SearchMode.SECONDARY ? client.getId() : null));
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadClientOrders(searchMode == SearchMode.SECONDARY ? client.getId() : null));
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadClientOrders(searchMode == SearchMode.SECONDARY ? client.getId() : null));
        searchParams.getFiltersProperty().addListener((MapChangeListener.Change<? extends String, ? extends String> change) -> {
            if (searchParams.getFiltersProperty().entrySet().size() == 1) { // Allow only one filter at a time
                loadClientOrders(searchMode == SearchMode.SECONDARY ? client.getId() : null);
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
        tableToolbarController.getDeleteSelectedRowsButton().setOnAction(e -> openConfirmDeleteDialog(selectedRowsIndices));
        tableToolbarController.getCreateNewShipmentButton().setOnAction(e -> addNewOrder());
    }

    private void setUpConfirmDialogListeners() {
        Consumer<List<ClientOrder>> onConfirmDelete = this::handleDeleteOrders;
        Runnable onCancelDelete = this::closeConfirmDeleteDialog;
        confirmDialogDeleteListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);

        Consumer<List<ClientOrder>> onConfirmUpdate = this::handleUpdateOrders;
        Runnable onCancelUpdate = this::closeConfirmUpdateDialog;
        confirmDialogUpdateListener = new RunnableConfirmDialogActionListener<>(onConfirmUpdate, onCancelUpdate);

        Consumer<List<ClientOrder>> onConfirmCreate = this::handleCreateOrders;
        Runnable onCancelCreate = this::closeConfirmCreateDialog;
        confirmDialogCreateListener = new RunnableConfirmDialogActionListener<>(onConfirmCreate, onCancelCreate);
    }

    private void setUpRowListeners(TableData<ClientOrder> clientOrder) {
        // Add listener to the selectedProperty
        clientOrder.isSelectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (Boolean.TRUE.equals(isSelected)) {
                selectedRowsIndices.add(tableView.getItems().indexOf(clientOrder));
            } else {
                selectedRowsIndices.remove(Integer.valueOf(tableView.getItems().indexOf(clientOrder)));
            }
            selectedCount.set(selectedRowsIndices.size());
        });
    }

    // Data loading
    private void loadClientOrders(Integer clientId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            fallbackManager.setLoading(false);
            return;
        }

        if (searchMode == SearchMode.SECONDARY) {
            if (clientId == null) return;
            clientOrdersService.getClientOrdersAdvanced(clientId, searchMode, searchParams)
                    .thenApply(this::handleOrdersResponse)
                    .exceptionally(this::handleOrdersException);
        } else {
            if (currentUser.getOrganization().getId() == null) return;
            clientOrdersService.getClientOrdersAdvanced(currentUser.getOrganization().getId(), searchMode, searchParams)
                    .thenApply(this::handleOrdersResponse)
                    .exceptionally(this::handleOrdersException);
        }
    }

    private Result<PaginatedResults<ClientOrder>> handleOrdersResponse(Result<PaginatedResults<ClientOrder>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("No orders found");
                return;
            }
            PaginatedResults<ClientOrder> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalRowsCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalRowsCount);

            tableView.getItems().clear();
            if (paginatedResults.results.isEmpty()) {
//                fallbackManager.setNoResults(true);
                return;
            }

            for (ClientOrder clientOrder : paginatedResults.results) {
                ClientOrder oldData = new ClientOrder(clientOrder);
                TableData<ClientOrder> tableRow = new TableData<>(clientOrder, oldData, new SimpleBooleanProperty(false));
                setUpRowListeners(tableRow);
                tableView.getItems().add(tableRow);
            }
        });

        return result;
    }

    private Result<PaginatedResults<ClientOrder>> handleOrdersException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load client orders."));
        return new Result<>();
    }

    // UI Actions
    private void addNewOrder() {
        isNewOrderMode.set(true);
        tableToolbarController.toggleButtonVisibilityOnCreate(isNewOrderMode.get());

        ClientOrder newOrder = new ClientOrder();
        TableData<ClientOrder> newOrderRow = new TableData<>(newOrder, newOrder, new SimpleBooleanProperty(false));
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
            TableData<ClientOrder> tableRow = tableView.getItems().get(index);
            ClientOrder oldOrder = new ClientOrder(tableRow.getData());
            oldOrder.setProduct(new Product(tableRow.getData().getProduct()));
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
            TableData<ClientOrder> tableRow = tableView.getItems().get(rowIndex);
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
                "Confirm Client Orders Create",
                "Are you sure you want to create new orders? This action cannot be undone.",
                null);
        List<ClientOrder> selectedOrders = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedOrders.add(tableView.getItems().get(index).getData());
        }
        confirmClientOrderCreateController.setData(selectedOrders, confirmDialogInput);
        toggleDialogVisibility(confirmCreateDialogContainer, true);
    }

    private void closeConfirmCreateDialog() {
        toggleDialogVisibility(confirmCreateDialogContainer, false);
    }

    private void openConfirmUpdateDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Client Orders Update",
                "Are you sure you want to update selected orders?",
                null);
        List<ClientOrder> selectedOrders = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedOrders.add(tableView.getItems().get(index).getData());
        }
        confirmClientOrderUpdateController.setData(selectedOrders, confirmDialogInput);
        toggleDialogVisibility(confirmUpdateDialogContainer, true);
    }

    private void closeConfirmUpdateDialog() {
        toggleDialogVisibility(confirmUpdateDialogContainer, false);
    }

    private void openConfirmDeleteDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Client Orders Delete",
                "Are you sure you want to delete selected orders?",
                null);
        List<ClientOrder> selectedOrders = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedOrders.add(tableView.getItems().get(index).getData());
        }
        confirmClientOrderDeleteController.setData(selectedOrders, confirmDialogInput);
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
    private void handleCreateOrders(List<ClientOrder> clientOrders) {
        List<CreateClientOrderDTO> createClientOrderDTOs = new ArrayList<>();
        for (ClientOrder order : clientOrders) {
            CreateClientOrderDTO createClientOrderDTO = getCreateClientOrderDTO(order);

            createClientOrderDTOs.add(createClientOrderDTO);
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientOrdersWriteService.createClientOrdersInBulk(createClientOrderDTOs)
                .thenApply(this::handleCreateClientOrdersResponse)
                .exceptionally(this::handleCreateClientOrdersException);
    }

    private CreateClientOrderDTO getCreateClientOrderDTO(ClientOrder order) {
        CreateClientOrderDTO createClientOrderDTO = new CreateClientOrderDTO();
        if (client == null || client.getOrganizationId() == null) {
            throw new IllegalArgumentException("Client Organization ID is missing");
        }
        createClientOrderDTO.setOrganizationId(client.getOrganizationId());
        createClientOrderDTO.setClientId(client.getId());
        createClientOrderDTO.setProductId(order.getProduct().getId());
        createClientOrderDTO.setQuantity(order.getQuantity());
        createClientOrderDTO.setDeliveredQuantity(order.getDeliveredQuantity());
        createClientOrderDTO.setOrderDate(order.getOrderDate());
        createClientOrderDTO.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        createClientOrderDTO.setDeliveryDate(order.getDeliveryDate());
        createClientOrderDTO.setStatus(order.getStatus());
        createClientOrderDTO.setCompanyId(order.getCompanyId());

        return createClientOrderDTO;
    }

    private Result<List<ClientOrder>> handleCreateClientOrdersResponse(Result<List<ClientOrder>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the Client Orders.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            isNewOrderMode.set(false);
            closeConfirmCreateDialog();
            updateUIOnSuccessfulOperation();
            ToastInfo toastInfo = new ToastInfo("Success", "Client Orders created successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<ClientOrder>> handleCreateClientOrdersException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the Client Orders.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void handleUpdateOrders(List<ClientOrder> clientOrders) {
        List<UpdateClientOrderDTO> updateClientOrderDTOs = new ArrayList<>();

        for (ClientOrder order : clientOrders) {
            UpdateClientOrderDTO updateClientOrderDTO = getUpdateClientOrderDTO(order);

            updateClientOrderDTOs.add(updateClientOrderDTO);
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientOrdersWriteService.updateClientOrdersInBulk(updateClientOrderDTOs)
                .thenApply(this::handleUpdateClientOrdersResponse)
                .exceptionally(this::handleUpdateClientOrdersException);
    }

    private UpdateClientOrderDTO getUpdateClientOrderDTO(ClientOrder order) {
        UpdateClientOrderDTO updateClientOrderDTO = new UpdateClientOrderDTO();
        updateClientOrderDTO.setId(order.getId());
        updateClientOrderDTO.setOrganizationId(order.getOrganizationId());
        updateClientOrderDTO.setProductId(order.getProduct().getId());
        updateClientOrderDTO.setQuantity(order.getQuantity());
        updateClientOrderDTO.setDeliveredQuantity(order.getDeliveredQuantity());
        updateClientOrderDTO.setOrderDate(order.getOrderDate());
        updateClientOrderDTO.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        updateClientOrderDTO.setDeliveryDate(order.getDeliveryDate());
        updateClientOrderDTO.setStatus(order.getStatus());
        updateClientOrderDTO.setCompanyId(order.getCompanyId());

        return updateClientOrderDTO;
    }

    private Result<List<ClientOrder>> handleUpdateClientOrdersResponse(Result<List<ClientOrder>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error updating the Client Orders.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            isNewOrderMode.set(false);
            closeConfirmUpdateDialog();
            updateUIOnSuccessfulOperation();
            ToastInfo toastInfo = new ToastInfo("Success", "Client Orders updated successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<ClientOrder>> handleUpdateClientOrdersException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error updating the Client Orders.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void handleDeleteOrders(List<ClientOrder> clientOrders) {
        List<Integer> ordersToRemoveIds = new ArrayList<>();
        for (ClientOrder order : clientOrders) {
            ordersToRemoveIds.add(order.getId());
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientOrdersWriteService.deleteClientOrderInBulk(ordersToRemoveIds)
                .thenApply(this::handleDeleteClientOrdersResponse)
                .exceptionally(this::handleDeleteClientOrdersException);
    }

    private Result<List<Integer>> handleDeleteClientOrdersResponse(Result<List<Integer>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error deleting the Client Orders.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            tableView.getItems().removeIf(tableData -> result.getData().contains(tableData.getData().getId()));

            closeConfirmDeleteDialog();
            isEditMode.set(false);
            tableView.refresh();
            selectedRowsIndices.clear();
            ToastInfo toastInfo = new ToastInfo("Success", "Client Orders deleted successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<Integer>> handleDeleteClientOrdersException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error deleting the Client Orders.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void updateUIOnSuccessfulOperation() {
        isEditMode.set(false);
        newOrderCount = 0;
        tableView.getSelectionModel().clearSelection();
        tableToolbarController.toggleButtonVisibilityOnCancel();

        List<Integer> indicesToClear = new ArrayList<>(selectedRowsIndices);
        for (Integer rowIndex : indicesToClear) {
            TableData<ClientOrder> tableRow = tableView.getItems().get(rowIndex);
            tableRow.setSelected(false);
        }

        selectRowColumn.setEditable(true);
        selectedRowsIndices.clear();
        tableView.refresh();
    }
}