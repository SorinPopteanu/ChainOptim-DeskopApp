package org.chainoptim.desktop.features.demand.clientshipment.controller;

import org.chainoptim.desktop.core.main.context.TenantContext;
import org.chainoptim.desktop.core.tenant.user.model.User;
import org.chainoptim.desktop.features.demand.clientshipment.dto.CreateClientShipmentDTO;
import org.chainoptim.desktop.features.demand.clientshipment.dto.UpdateClientShipmentDTO;
import org.chainoptim.desktop.features.demand.client.model.Client;
import org.chainoptim.desktop.features.demand.clientshipment.model.ClientShipment;
import org.chainoptim.desktop.features.demand.clientshipment.service.ClientShipmentsService;
import org.chainoptim.desktop.features.demand.clientshipment.service.ClientShipmentsWriteService;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.enums.ShipmentStatus;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.features.location.model.Location;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.*;
import org.chainoptim.desktop.shared.table.TableToolbarController;
import org.chainoptim.desktop.shared.table.edit.cell.ComboBoxEditableCell;
import org.chainoptim.desktop.shared.table.edit.cell.DateTimePickerCell;
import org.chainoptim.desktop.shared.table.edit.cell.EditableCell;
import org.chainoptim.desktop.shared.table.model.TableData;
import org.chainoptim.desktop.shared.table.util.TableConfigurer;
import org.chainoptim.desktop.shared.table.util.SelectComponentLoader;
import org.chainoptim.desktop.shared.common.ui.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.common.ui.toast.model.ToastInfo;
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

public class ClientShipmentsController implements DataReceiver<SearchData<Client>> {

    // Services
    private final ClientShipmentsService clientShipmentsService;
    private final ClientShipmentsWriteService clientShipmentsWriteService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;

    // Controllers
    private TableToolbarController tableToolbarController;
    private PageSelectorController pageSelectorController;
    private final SelectComponentLoader selectComponentLoader;
    private GenericConfirmDialogController<List<ClientShipment>> confirmClientShipmentUpdateController;
    private GenericConfirmDialogController<List<ClientShipment>> confirmClientShipmentDeleteController;
    private GenericConfirmDialogController<List<ClientShipment>> confirmClientShipmentCreateController;

    // State
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;

    private SearchMode searchMode;
    private Client client;
    private final List<ShipmentStatus> statusOptions = Arrays.asList(ShipmentStatus.values());
    private long totalRowsCount;
    private int newShipmentCount = 0;
    private final List<Integer> selectedRowsIndices = new ArrayList<>();
    private final SimpleIntegerProperty selectedCount = new SimpleIntegerProperty(0);
    private final BooleanProperty isEditMode = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isNewShipmentMode = new SimpleBooleanProperty(false);

    // Confirm Dialog Listeners
    private RunnableConfirmDialogActionListener<List<ClientShipment>> confirmDialogUpdateListener;
    private RunnableConfirmDialogActionListener<List<ClientShipment>> confirmDialogDeleteListener;
    private RunnableConfirmDialogActionListener<List<ClientShipment>> confirmDialogCreateListener;

    // FXML
    @FXML
    private StackPane tableToolbarContainer;
    @FXML
    private ScrollPane clientShipmentsScrollPane;
    @FXML
    private TableView<TableData<ClientShipment>> tableView;
    @FXML
    private TableColumn<TableData<ClientShipment>, Boolean> selectRowColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, Integer> shipmentIdColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, Integer> clientOrderIdColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, Float> quantityColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, LocalDateTime> shipmentStartingDateColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, LocalDateTime> estimatedArrivalDateColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, LocalDateTime> arrivalDateColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, String> transporterTypeColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, ShipmentStatus> statusColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, Location> sourceLocationColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, Location> destinationLocationColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, Float> currentLocationLatitudeColumn;
    @FXML
    private TableColumn<TableData<ClientShipment>, Float> currentLocationLongitudeColumn;
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
    public ClientShipmentsController(ClientShipmentsService clientShipmentsService,
                                       ClientShipmentsWriteService clientShipmentsWriteService,
                                       CommonViewsLoader commonViewsLoader,
                                       SelectComponentLoader selectComponentLoader,
                                       ToastManager toastManager,
                                       FallbackManager fallbackManager,
                                       SearchParams searchParams) {
        this.clientShipmentsService = clientShipmentsService;
        this.clientShipmentsWriteService = clientShipmentsWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.selectComponentLoader = selectComponentLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void setData(SearchData<Client> searchData) {
        this.client = searchData.getData();
        this.searchMode = searchData.getSearchMode();

        searchParams.setItemsPerPage(20);
        SearchOptions searchOptions = SearchOptionsConfiguration.getSearchOptions(Feature.CLIENT_SHIPMENT);

        commonViewsLoader.loadFallbackManager(fallbackContainer);
        tableToolbarController = commonViewsLoader.initializeTableToolbar(tableToolbarContainer);
        tableToolbarController.initialize(new ListHeaderParams
                (searchMode, searchParams,
                        "Client Shipments", "/img/box-solid.png", Feature.CLIENT_SHIPMENT,
                        searchOptions.getSortOptions(), searchOptions.getFilterOptions(),
                        () -> loadClientShipments(searchMode == SearchMode.SECONDARY ? client.getId() : null), null, null));
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
        selectComponentLoader.initialize();

        TableConfigurer.configureTableView(tableView, selectRowColumn);
        configureTableColumns();
        setUpListeners();
        loadConfirmDialogs();

        loadClientShipments(searchMode == SearchMode.SECONDARY ? client.getId() : null);
    }

    // Loading
    private void loadConfirmDialogs() {
        confirmClientShipmentCreateController = commonViewsLoader.loadConfirmDialog(confirmCreateDialogContainer);
        confirmClientShipmentCreateController.setActionListener(confirmDialogCreateListener);
        closeConfirmCreateDialog();

        confirmClientShipmentUpdateController = commonViewsLoader.loadConfirmDialog(confirmUpdateDialogContainer);
        confirmClientShipmentUpdateController.setActionListener(confirmDialogUpdateListener);
        closeConfirmUpdateDialog();

        confirmClientShipmentDeleteController = commonViewsLoader.loadConfirmDialog(confirmDeleteDialogContainer);
        confirmClientShipmentDeleteController.setActionListener(confirmDialogDeleteListener);
        closeConfirmDeleteDialog();
    }

    // Configuration
    // - Table columns
    private void configureTableColumns() {
        // Bind columns to data
        selectRowColumn.setCellValueFactory(data -> data.getValue().isSelectedProperty());
        shipmentIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getId()));
        clientOrderIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getClientOrderId()));
        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getQuantity()));
        shipmentStartingDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getShipmentStartingDate()));
        estimatedArrivalDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getEstimatedArrivalDate()));
        arrivalDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getArrivalDate()));
        transporterTypeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getTransporterType()));
        statusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getStatus()));
        sourceLocationColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getSourceLocation()));
        destinationLocationColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getDestinationLocation()));
        currentLocationLatitudeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getCurrentLocationLatitude()));
        currentLocationLongitudeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getCurrentLocationLongitude()));

        configureColumnCellFactories();
    }

    private void configureColumnCellFactories() {
        quantityColumn.setCellFactory(column -> new EditableCell<TableData<ClientShipment>, Float>(
                isEditMode, selectedRowsIndices, Float::parseFloat) {
            @Override
            protected void commitChange(TableData<ClientShipment> item, Float newValue) {
                item.getData().setQuantity(newValue);
            }
        });
        shipmentStartingDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<ClientShipment>, LocalDateTime>(
                isEditMode, selectedRowsIndices, true) {
            @Override
            protected void commitChange(TableData<ClientShipment> item, LocalDateTime newValue) {
                item.getData().setShipmentStartingDate(newValue);
            }
        });
        estimatedArrivalDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<ClientShipment>, LocalDateTime>(
                isEditMode, selectedRowsIndices, true) {
            @Override
            protected void commitChange(TableData<ClientShipment> item, LocalDateTime newValue) {
                item.getData().setEstimatedArrivalDate(newValue);
            }
        });
        arrivalDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<ClientShipment>, LocalDateTime>(
                isEditMode, selectedRowsIndices, true) {
            @Override
            protected void commitChange(TableData<ClientShipment> item, LocalDateTime newValue) {
                item.getData().setArrivalDate(newValue);
            }
        });
        statusColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<ClientShipment>, ShipmentStatus>(
                isEditMode, selectedRowsIndices, null, statusOptions) {
            @Override
            protected void commitChange(TableData<ClientShipment> item, ShipmentStatus newValue) {
                item.getData().setStatus(newValue);
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
            clientShipmentsScrollPane.setVisible(newValue);
            clientShipmentsScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void setUpSearchListeners() {
        searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadClientShipments(searchMode == SearchMode.SECONDARY ? client.getId() : null));
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadClientShipments(searchMode == SearchMode.SECONDARY ? client.getId() : null));
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadClientShipments(searchMode == SearchMode.SECONDARY ? client.getId() : null));
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadClientShipments(searchMode == SearchMode.SECONDARY ? client.getId() : null));
        searchParams.getFiltersProperty().addListener((MapChangeListener.Change<? extends String, ? extends String> change) -> {
            if (searchParams.getFiltersProperty().entrySet().size() == 1) { // Allow only one filter at a time
                loadClientShipments(searchMode == SearchMode.SECONDARY ? client.getId() : null);
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
            if (isNewShipmentMode.get()) {
                openConfirmCreateDialog();
            } else {
                openConfirmUpdateDialog(selectedRowsIndices);
            }
        });
        tableToolbarController.getDeleteSelectedRowsButton().setOnAction(e -> openConfirmDeleteDialog(selectedRowsIndices));
        tableToolbarController.getCreateNewShipmentButton().setOnAction(e -> addNewShipment());
    }

    private void setUpConfirmDialogListeners() {
        Consumer<List<ClientShipment>> onConfirmDelete = this::handleDeleteShipments;
        Runnable onCancelDelete = this::closeConfirmDeleteDialog;
        confirmDialogDeleteListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);

        Consumer<List<ClientShipment>> onConfirmUpdate = this::handleUpdateShipments;
        Runnable onCancelUpdate = this::closeConfirmUpdateDialog;
        confirmDialogUpdateListener = new RunnableConfirmDialogActionListener<>(onConfirmUpdate, onCancelUpdate);

        Consumer<List<ClientShipment>> onConfirmCreate = this::handleCreateShipments;
        Runnable onCancelCreate = this::closeConfirmCreateDialog;
        confirmDialogCreateListener = new RunnableConfirmDialogActionListener<>(onConfirmCreate, onCancelCreate);
    }

    private void setUpRowListeners(TableData<ClientShipment> clientShipment) {
        // Add listener to the selectedProperty
        clientShipment.isSelectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (Boolean.TRUE.equals(isSelected)) {
                selectedRowsIndices.add(tableView.getItems().indexOf(clientShipment));
            } else {
                selectedRowsIndices.remove(Integer.valueOf(tableView.getItems().indexOf(clientShipment)));
            }
            selectedCount.set(selectedRowsIndices.size());
        });
    }

    // Data loading
    private void loadClientShipments(Integer clientId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            fallbackManager.setLoading(false);
            return;
        }

        if (searchMode == SearchMode.SECONDARY) {
            if (clientId == null) return;
            clientShipmentsService.getClientShipmentsAdvanced(1, searchMode, searchParams)
                    .thenApply(this::handleShipmentsResponse)
                    .exceptionally(this::handleShipmentsException);
        } else {
            if (currentUser.getOrganization().getId() == null) return;
            clientShipmentsService.getClientShipmentsAdvanced(currentUser.getOrganization().getId(), searchMode, searchParams)
                    .thenApply(this::handleShipmentsResponse)
                    .exceptionally(this::handleShipmentsException);
        }
    }

    private Result<PaginatedResults<ClientShipment>> handleShipmentsResponse(Result<PaginatedResults<ClientShipment>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("No shipments found");
                return;
            }
            PaginatedResults<ClientShipment> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalRowsCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalRowsCount);

            tableView.getItems().clear();
            if (paginatedResults.results.isEmpty()) {
//                fallbackManager.setNoResults(true);
                return;
            }

            for (ClientShipment clientShipment : paginatedResults.results) {
                ClientShipment oldData = new ClientShipment(clientShipment);
                TableData<ClientShipment> tableRow = new TableData<>(clientShipment, oldData, new SimpleBooleanProperty(false));
                setUpRowListeners(tableRow);
                tableView.getItems().add(tableRow);
            }
        });

        return result;
    }

    private Result<PaginatedResults<ClientShipment>> handleShipmentsException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load client shipments."));
        return new Result<>();
    }

    // UI Actions
    private void addNewShipment() {
        isNewShipmentMode.set(true);
        tableToolbarController.toggleButtonVisibilityOnCreate(isNewShipmentMode.get());

        ClientShipment newShipment = new ClientShipment();
        TableData<ClientShipment> newShipmentRow = new TableData<>(newShipment, newShipment, new SimpleBooleanProperty(false));
        tableView.getItems().addFirst(newShipmentRow);
        newShipmentRow.setSelected(true);

        selectedRowsIndices.clear();
        for (int i = 0; i <= newShipmentCount; i++) {
            selectedRowsIndices.add(i);
        }
        newShipmentCount++;
        isEditMode.set(true);
        selectRowColumn.setEditable(false);
        tableView.refresh();
    }

    private void editSelectedRows() {
        tableToolbarController.toggleButtonVisibilityOnEdit(true);
        isEditMode.set(true);
        for (Integer index : selectedRowsIndices) {
            TableData<ClientShipment> tableRow = tableView.getItems().get(index);
            ClientShipment oldShipment = new ClientShipment(tableRow.getData());
            tableRow.setOldData(oldShipment);
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
            TableData<ClientShipment> tableRow = tableView.getItems().get(rowIndex);
            tableRow.setData(tableRow.getOldData());
            tableRow.setSelected(false);
        }
        selectedRowsIndices.clear();

        // Delete created new shipments
        if (isNewShipmentMode.get()) {
            for (int i = 0; i < newShipmentCount; i++) {
                tableView.getItems().removeFirst();
            }
            isNewShipmentMode.set(false);
            newShipmentCount = 0;
        }
        selectRowColumn.setEditable(true);
        tableView.refresh();
    }

    // Confirm Dialogs
    private void openConfirmCreateDialog() {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Client Shipments Create",
                "Are you sure you want to create new shipments?",
                null);
        List<ClientShipment> selectedShipments = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedShipments.add(tableView.getItems().get(index).getData());
        }
        confirmClientShipmentCreateController.setData(selectedShipments, confirmDialogInput);
        toggleDialogVisibility(confirmCreateDialogContainer, true);
    }

    private void closeConfirmCreateDialog() {
        toggleDialogVisibility(confirmCreateDialogContainer, false);
    }

    private void openConfirmUpdateDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Client Shipments Update",
                "Are you sure you want to update selected shipments?",
                null);
        List<ClientShipment> selectedShipments = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedShipments.add(tableView.getItems().get(index).getData());
        }
        confirmClientShipmentUpdateController.setData(selectedShipments, confirmDialogInput);
        toggleDialogVisibility(confirmUpdateDialogContainer, true);
    }

    private void closeConfirmUpdateDialog() {
        toggleDialogVisibility(confirmUpdateDialogContainer, false);
    }

    private void openConfirmDeleteDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Client Shipments Delete",
                "Are you sure you want to delete selected shipments? This action cannot be undone.",
                null);
        List<ClientShipment> selectedShipments = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedShipments.add(tableView.getItems().get(index).getData());
        }
        confirmClientShipmentDeleteController.setData(selectedShipments, confirmDialogInput);
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
    private void handleCreateShipments(List<ClientShipment> clientShipments) {
        List<CreateClientShipmentDTO> createClientShipmentDTOs = new ArrayList<>();
        for (ClientShipment shipment : clientShipments) {
            CreateClientShipmentDTO createClientShipmentDTO = getCreateClientShipmentDTO(shipment);

            createClientShipmentDTOs.add(createClientShipmentDTO);
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientShipmentsWriteService.createClientShipmentsInBulk(createClientShipmentDTOs)
                .thenApply(this::handleCreateClientShipmentsResponse)
                .exceptionally(this::handleCreateClientShipmentsException);
    }

    private CreateClientShipmentDTO getCreateClientShipmentDTO(ClientShipment shipment) {
        CreateClientShipmentDTO createClientShipmentDTO = new CreateClientShipmentDTO();
        if (client == null || client.getOrganizationId() == null) {
            throw new IllegalArgumentException("Client Organization ID is missing");
        }
        createClientShipmentDTO.setOrganizationId(client.getOrganizationId());
        createClientShipmentDTO.setClientOrderId(1);
        createClientShipmentDTO.setQuantity(shipment.getQuantity());
        createClientShipmentDTO.setShipmentStartingDate(shipment.getShipmentStartingDate());
        createClientShipmentDTO.setEstimatedArrivalDate(shipment.getEstimatedArrivalDate());
        createClientShipmentDTO.setArrivalDate(shipment.getArrivalDate());
        createClientShipmentDTO.setStatus(shipment.getStatus());
        createClientShipmentDTO.setSourceLocation(shipment.getSourceLocation());
        createClientShipmentDTO.setDestinationLocation(shipment.getDestinationLocation());
        createClientShipmentDTO.setCurrentLocationLatitude(shipment.getCurrentLocationLatitude());
        createClientShipmentDTO.setCurrentLocationLongitude(shipment.getCurrentLocationLongitude());

        return createClientShipmentDTO;
    }

    private Result<List<ClientShipment>> handleCreateClientShipmentsResponse(Result<List<ClientShipment>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the Client Shipments.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            isNewShipmentMode.set(false);
            closeConfirmCreateDialog();
            updateUIOnSuccessfulOperation();
            ToastInfo toastInfo = new ToastInfo("Success", "Client Shipments created successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<ClientShipment>> handleCreateClientShipmentsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the Client Shipments.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void handleUpdateShipments(List<ClientShipment> clientShipments) {
        List<UpdateClientShipmentDTO> updateClientShipmentDTOs = new ArrayList<>();

        for (ClientShipment shipment : clientShipments) {
            UpdateClientShipmentDTO updateClientShipmentDTO = getUpdateClientShipmentDTO(shipment);

            updateClientShipmentDTOs.add(updateClientShipmentDTO);
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientShipmentsWriteService.updateClientShipmentsInBulk(updateClientShipmentDTOs)
                .thenApply(this::handleUpdateClientShipmentsResponse)
                .exceptionally(this::handleUpdateClientShipmentsException);
    }

    private UpdateClientShipmentDTO getUpdateClientShipmentDTO(ClientShipment shipment) {
        UpdateClientShipmentDTO updateClientShipmentDTO = new UpdateClientShipmentDTO();
        updateClientShipmentDTO.setId(shipment.getId());
        updateClientShipmentDTO.setOrganizationId(client.getOrganizationId());
        updateClientShipmentDTO.setClientOrderId(shipment.getClientOrderId());
        updateClientShipmentDTO.setQuantity(shipment.getQuantity());
        updateClientShipmentDTO.setShipmentStartingDate(shipment.getShipmentStartingDate());
        updateClientShipmentDTO.setEstimatedArrivalDate(shipment.getEstimatedArrivalDate());
        updateClientShipmentDTO.setArrivalDate(shipment.getArrivalDate());
        updateClientShipmentDTO.setStatus(shipment.getStatus());
        updateClientShipmentDTO.setSourceLocation(shipment.getSourceLocation());
        updateClientShipmentDTO.setDestinationLocation(shipment.getDestinationLocation());
        updateClientShipmentDTO.setCurrentLocationLatitude(shipment.getCurrentLocationLatitude());
        updateClientShipmentDTO.setCurrentLocationLongitude(shipment.getCurrentLocationLongitude());

        return updateClientShipmentDTO;
    }

    private Result<List<ClientShipment>> handleUpdateClientShipmentsResponse(Result<List<ClientShipment>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error updating the Client Shipments.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            isNewShipmentMode.set(false);
            closeConfirmUpdateDialog();
            updateUIOnSuccessfulOperation();
            ToastInfo toastInfo = new ToastInfo("Success", "Client Shipments updated successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<ClientShipment>> handleUpdateClientShipmentsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error updating the Client Shipments.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void handleDeleteShipments(List<ClientShipment> clientShipments) {
        List<Integer> shipmentsToRemoveIds = new ArrayList<>();
        for (ClientShipment shipment : clientShipments) {
            shipmentsToRemoveIds.add(shipment.getId());
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientShipmentsWriteService.deleteClientShipmentInBulk(shipmentsToRemoveIds)
                .thenApply(this::handleDeleteClientShipmentsResponse)
                .exceptionally(this::handleDeleteClientShipmentsException);
    }

    private Result<List<Integer>> handleDeleteClientShipmentsResponse(Result<List<Integer>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error deleting the Client Shipments.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            tableView.getItems().removeIf(tableData -> result.getData().contains(tableData.getData().getId()));

            closeConfirmDeleteDialog();
            isEditMode.set(false);
            tableView.refresh();
            selectedRowsIndices.clear();
            ToastInfo toastInfo = new ToastInfo("Success", "Client Shipments deleted successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<Integer>> handleDeleteClientShipmentsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error deleting the Client Shipments.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void updateUIOnSuccessfulOperation() {
        isEditMode.set(false);
        newShipmentCount = 0;
        tableView.getSelectionModel().clearSelection();
        tableToolbarController.toggleButtonVisibilityOnCancel();

        List<Integer> indicesToClear = new ArrayList<>(selectedRowsIndices);
        for (Integer rowIndex : indicesToClear) {
            TableData<ClientShipment> tableRow = tableView.getItems().get(rowIndex);
            tableRow.setSelected(false);
        }

        selectRowColumn.setEditable(true);
        selectedRowsIndices.clear();
        tableView.refresh();
    }
}