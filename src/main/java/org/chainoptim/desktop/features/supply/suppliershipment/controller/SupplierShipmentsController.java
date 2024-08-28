package org.chainoptim.desktop.features.supply.suppliershipment.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.goods.component.model.Component;
import org.chainoptim.desktop.features.supply.suppliershipment.dto.CreateSupplierShipmentDTO;
import org.chainoptim.desktop.features.supply.suppliershipment.dto.UpdateSupplierShipmentDTO;
import org.chainoptim.desktop.features.supply.supplier.model.Supplier;
import org.chainoptim.desktop.features.supply.suppliershipment.model.SupplierShipment;
import org.chainoptim.desktop.features.supply.suppliershipment.service.SupplierShipmentsService;
import org.chainoptim.desktop.features.supply.suppliershipment.service.SupplierShipmentsWriteService;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
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
import org.chainoptim.desktop.shared.table.util.SelectComponentLoader;
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
import java.util.List;
import java.util.function.Consumer;

public class SupplierShipmentsController implements DataReceiver<SearchData<Supplier>> {

    // Services
    private final SupplierShipmentsService supplierShipmentsService;
    private final SupplierShipmentsWriteService supplierShipmentsWriteService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;

    // Controllers
    private TableToolbarController tableToolbarController;
    private PageSelectorController pageSelectorController;
    private final SelectComponentLoader selectComponentLoader;
    private GenericConfirmDialogController<List<SupplierShipment>> confirmSupplierShipmentUpdateController;
    private GenericConfirmDialogController<List<SupplierShipment>> confirmSupplierShipmentDeleteController;
    private GenericConfirmDialogController<List<SupplierShipment>> confirmSupplierShipmentCreateController;

    // State
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;

    private SearchMode searchMode;
    private Supplier supplier;
    private long totalRowsCount;
    private int newShipmentCount = 0;
    private final List<Integer> selectedRowsIndices = new ArrayList<>();
    private final SimpleIntegerProperty selectedCount = new SimpleIntegerProperty(0);
    private final BooleanProperty isEditMode = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isNewShipmentMode = new SimpleBooleanProperty(false);

    // Confirm Dialog Listeners
    private RunnableConfirmDialogActionListener<List<SupplierShipment>> confirmDialogUpdateListener;
    private RunnableConfirmDialogActionListener<List<SupplierShipment>> confirmDialogDeleteListener;
    private RunnableConfirmDialogActionListener<List<SupplierShipment>> confirmDialogCreateListener;

    // FXML
    @FXML
    private StackPane tableToolbarContainer;
    @FXML
    private ScrollPane supplierShipmentsScrollPane;
    @FXML
    private TableView<TableData<SupplierShipment>> tableView;
    @FXML
    private TableColumn<TableData<SupplierShipment>, Boolean> selectRowColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, Integer> shipmentIdColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, String> companyIdColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, String> supplierNameColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, String> componentNameColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, Float> quantityColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, Float> deliveredQuantityColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, String> statusColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, Integer> supplierOrderIdColumn;
    private TableColumn<TableData<SupplierShipment>, LocalDateTime> shipmentStartingDateColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, LocalDateTime> estimatedArrivalDateColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, LocalDateTime> arrivalDateColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, String> transporterTypeColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, Location> sourceLocationColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, Location> destinationLocationColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, Float> currentLocationLatitudeColumn;
    @FXML
    private TableColumn<TableData<SupplierShipment>, Float> currentLocationLongitudeColumn;
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
    public SupplierShipmentsController(SupplierShipmentsService supplierShipmentsService,
                                       SupplierShipmentsWriteService supplierShipmentsWriteService,
                                       CommonViewsLoader commonViewsLoader,
                                       SelectComponentLoader selectComponentLoader,
                                       ToastManager toastManager,
                                       FallbackManager fallbackManager,
                                       SearchParams searchParams) {
        this.supplierShipmentsService = supplierShipmentsService;
        this.supplierShipmentsWriteService = supplierShipmentsWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.selectComponentLoader = selectComponentLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void setData(SearchData<Supplier> searchData) {
        this.supplier = searchData.getData();
        this.searchMode = searchData.getSearchMode();

        loadComponents();

        TableConfigurer.configureTableView(tableView, selectRowColumn);
        configureTableColumns();

        setUpListeners();
        loadConfirmDialogs();

        loadSupplierShipments(searchMode == SearchMode.SECONDARY ? supplier.getId() : null);
    }

    // Loading
    private void loadComponents() {
        searchParams.setItemsPerPage(20);
        SearchOptions searchOptions = SearchOptionsConfiguration.getSearchOptions(Feature.SUPPLIER_ORDER);

        commonViewsLoader.loadFallbackManager(fallbackContainer);
        tableToolbarController = commonViewsLoader.initializeTableToolbar(tableToolbarContainer);
        tableToolbarController.initialize(new ListHeaderParams
                (searchMode, searchParams,
                        "Supplier Shipments", "/img/box-solid.png", Feature.SUPPLIER_ORDER,
                        searchOptions.getSortOptions(), searchOptions.getFilterOptions(),
                        () -> loadSupplierShipments(searchMode == SearchMode.SECONDARY ? supplier.getId() : null), null, null));
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
        selectComponentLoader.initialize();
    }

    private void loadConfirmDialogs() {
        confirmSupplierShipmentCreateController = commonViewsLoader.loadConfirmDialog(confirmCreateDialogContainer);
        confirmSupplierShipmentCreateController.setActionListener(confirmDialogCreateListener);
        closeConfirmCreateDialog();

        confirmSupplierShipmentUpdateController = commonViewsLoader.loadConfirmDialog(confirmUpdateDialogContainer);
        confirmSupplierShipmentUpdateController.setActionListener(confirmDialogUpdateListener);
        closeConfirmUpdateDialog();

        confirmSupplierShipmentDeleteController = commonViewsLoader.loadConfirmDialog(confirmDeleteDialogContainer);
        confirmSupplierShipmentDeleteController.setActionListener(confirmDialogDeleteListener);
        closeConfirmDeleteDialog();
    }

    // Configuration
    // - Table columns
    private void configureTableColumns() {
        // Bind columns to data
        selectRowColumn.setCellValueFactory(data -> data.getValue().isSelectedProperty());
        shipmentIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getId()));
        companyIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getCompanyId() != null ? data.getValue().getData().getCompanyId() : "N/A"));
        supplierNameColumn.setCellValueFactory(data -> new SimpleObjectProperty<>("this.supplier.getName()"));
        componentNameColumn.setCellValueFactory(data -> {
            String componentName = data.getValue().getData().getComponentName();
            return new SimpleObjectProperty<>(componentName);
        });
        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getQuantity()));
        deliveredQuantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getDeliveredQuantity()));
        statusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getStatus()));
//        shipmentStartingDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getShipmentStartingDate()));
        estimatedArrivalDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getEstimatedArrivalDate()));
        arrivalDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData().getArrivalDate()));
        configureColumnCellFactories();
    }

    private void configureColumnCellFactories() {
        companyIdColumn.setCellFactory(column -> new EditableCell<TableData<SupplierShipment>, String>(
                isEditMode, selectedRowsIndices, String::toString) {
            @Override
            protected void commitChange(TableData<SupplierShipment> item, String newValue) {
                item.getData().setCompanyId(newValue);
            }
        });
        quantityColumn.setCellFactory(column -> new EditableCell<TableData<SupplierShipment>, Float>(
                isEditMode, selectedRowsIndices, Float::parseFloat) {
            @Override
            protected void commitChange(TableData<SupplierShipment> item, Float newValue) {
                item.getData().setQuantity(newValue);
            }
        });
        deliveredQuantityColumn.setCellFactory(column -> new EditableCell<TableData<SupplierShipment>, Float>(
                isEditMode, selectedRowsIndices, Float::parseFloat) {
                    @Override
                    protected void commitChange(TableData<SupplierShipment> item, Float newValue) {
                        item.getData().setDeliveredQuantity(newValue);
//                        shipmentStartingDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<SupplierShipment>, LocalDateTime>(
//                                isEditMode, selectedRowsIndices, true) {
//                            @Override
//                            protected void commitChange(TableData<SupplierShipment> item, LocalDateTime newValue) {
//                                item.getData().setShipmentStartingDate(newValue);
//                            }
//                        });
                        estimatedArrivalDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<SupplierShipment>, LocalDateTime>(
                                isEditMode, selectedRowsIndices, true) {
                            @Override
                            protected void commitChange(TableData<SupplierShipment> item, LocalDateTime newValue) {
                                item.getData().setEstimatedArrivalDate(newValue);
                            }
                        });
                        arrivalDateColumn.setCellFactory(column -> new DateTimePickerCell<TableData<SupplierShipment>, LocalDateTime>(
                                isEditMode, selectedRowsIndices, true) {
                            @Override
                            protected void commitChange(TableData<SupplierShipment> item, LocalDateTime newValue) {
                                item.getData().setArrivalDate(newValue);
                            }
                        });

//                        statusColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<SupplierShipment>, ShipmentStatus>(
//                                isEditMode, selectedRowsIndices, null, statusOptions) {
//                            @Override
//                            protected void commitChange(TableData<SupplierShipment> item, ShipmentStatus newValue) {
//                                item.getData().setStatus(newValue);
//                            }
//                        });
                        componentNameColumn.setCellFactory(column -> new ComboBoxEditableCell<TableData<SupplierShipment>, String>(
                                isEditMode, selectedRowsIndices, null, selectComponentLoader.getComponentsName()) {
                            @Override
                            protected void commitChange(TableData<SupplierShipment> item, String newValue) {
                                Component component = new Component();
                                component.setId(selectComponentLoader.getComponentIdByName(newValue));
                                component.setName(newValue);
                                item.getData().setComponentName(component.getName());
                            }
                        });
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
            supplierShipmentsScrollPane.setVisible(newValue);
            supplierShipmentsScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void setUpSearchListeners() {
        searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadSupplierShipments(searchMode == SearchMode.SECONDARY ? supplier.getId() : null));
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadSupplierShipments(searchMode == SearchMode.SECONDARY ? supplier.getId() : null));
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadSupplierShipments(searchMode == SearchMode.SECONDARY ? supplier.getId() : null));
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadSupplierShipments(searchMode == SearchMode.SECONDARY ? supplier.getId() : null));
        searchParams.getFiltersProperty().addListener((MapChangeListener.Change<? extends String, ? extends String> change) -> {
            if (searchParams.getFiltersProperty().entrySet().size() == 1) { // Allow only one filter at a time
                loadSupplierShipments(searchMode == SearchMode.SECONDARY ? supplier.getId() : null);
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
        Consumer<List<SupplierShipment>> onConfirmDelete = this::handleDeleteShipments;
        Runnable onCancelDelete = this::closeConfirmDeleteDialog;
        confirmDialogDeleteListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);

        Consumer<List<SupplierShipment>> onConfirmUpdate = this::handleUpdateShipments;
        Runnable onCancelUpdate = this::closeConfirmUpdateDialog;
        confirmDialogUpdateListener = new RunnableConfirmDialogActionListener<>(onConfirmUpdate, onCancelUpdate);

        Consumer<List<SupplierShipment>> onConfirmCreate = this::handleCreateShipments;
        Runnable onCancelCreate = this::closeConfirmCreateDialog;
        confirmDialogCreateListener = new RunnableConfirmDialogActionListener<>(onConfirmCreate, onCancelCreate);
    }

    private void setUpRowListeners(TableData<SupplierShipment> supplierShipment) {
        // Add listener to the selectedProperty
        supplierShipment.isSelectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (Boolean.TRUE.equals(isSelected)) {
                selectedRowsIndices.add(tableView.getItems().indexOf(supplierShipment));
            } else {
                selectedRowsIndices.remove(Integer.valueOf(tableView.getItems().indexOf(supplierShipment)));
            }
            selectedCount.set(selectedRowsIndices.size());
        });
    }

    // Data loading
    private void loadSupplierShipments(Integer supplierId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            fallbackManager.setLoading(false);
            return;
        }

        if (searchMode == SearchMode.SECONDARY) {
            if (supplierId == null) return;
            supplierShipmentsService.getSupplierShipmentsAdvanced(supplierId, searchMode, searchParams)
                    .thenApply(this::handleShipmentsResponse)
                    .exceptionally(this::handleShipmentsException);
        } else {
            if (currentUser.getOrganization().getId() == null) return;
            supplierShipmentsService.getSupplierShipmentsAdvanced(currentUser.getOrganization().getId(), searchMode, searchParams)
                    .thenApply(this::handleShipmentsResponse)
                    .exceptionally(this::handleShipmentsException);
        }
    }

    private Result<PaginatedResults<SupplierShipment>> handleShipmentsResponse(Result<PaginatedResults<SupplierShipment>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("No shipments found");
                return;
            }
            PaginatedResults<SupplierShipment> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalRowsCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalRowsCount);

            tableView.getItems().clear();
            if (paginatedResults.results.isEmpty()) {
//                fallbackManager.setNoResults(true);
                return;
            }

            for (SupplierShipment supplierShipment : paginatedResults.results) {
                SupplierShipment oldData = supplierShipment.deepCopy();
                TableData<SupplierShipment> tableRow = new TableData<>(supplierShipment, oldData, new SimpleBooleanProperty(false));
                setUpRowListeners(tableRow);
                tableView.getItems().add(tableRow);
            }
        });

        return result;
    }

    private Result<PaginatedResults<SupplierShipment>> handleShipmentsException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supplier shipments."));
        return new Result<>();
    }

    // UI Actions
    private void addNewShipment() {
        isNewShipmentMode.set(true);
        tableToolbarController.toggleButtonVisibilityOnCreate(isNewShipmentMode.get());

        SupplierShipment newShipment = new SupplierShipment();
        TableData<SupplierShipment> newShipmentRow = new TableData<>(newShipment, newShipment, new SimpleBooleanProperty(false));
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
            TableData<SupplierShipment> tableRow = tableView.getItems().get(index);
            SupplierShipment oldShipment = tableRow.getData().deepCopy();
            oldShipment.setComponentName(tableRow.getData().getComponentName());
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
            TableData<SupplierShipment> tableRow = tableView.getItems().get(rowIndex);
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
                "Confirm Supplier Shipments Create",
                "Are you sure you want to create new shipments?",
                null);
        List<SupplierShipment> selectedShipments = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedShipments.add(tableView.getItems().get(index).getData());
        }
        confirmSupplierShipmentCreateController.setData(selectedShipments, confirmDialogInput);
        toggleDialogVisibility(confirmCreateDialogContainer, true);
    }

    private void closeConfirmCreateDialog() {
        toggleDialogVisibility(confirmCreateDialogContainer, false);
    }

    private void openConfirmUpdateDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Supplier Shipments Update",
                "Are you sure you want to update selected shipments?",
                null);
        List<SupplierShipment> selectedShipments = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedShipments.add(tableView.getItems().get(index).getData());
        }
        confirmSupplierShipmentUpdateController.setData(selectedShipments, confirmDialogInput);
        toggleDialogVisibility(confirmUpdateDialogContainer, true);
    }

    private void closeConfirmUpdateDialog() {
        toggleDialogVisibility(confirmUpdateDialogContainer, false);
    }

    private void openConfirmDeleteDialog(List<Integer> selectedRowsIndices) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Supplier Shipments Delete",
                "Are you sure you want to delete selected shipments? This action cannot be undone.",
                null);
        List<SupplierShipment> selectedShipments = new ArrayList<>();
        for (Integer index : selectedRowsIndices) {
            selectedShipments.add(tableView.getItems().get(index).getData());
        }
        confirmSupplierShipmentDeleteController.setData(selectedShipments, confirmDialogInput);
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
    private void handleCreateShipments(List<SupplierShipment> supplierShipments) {
        List<CreateSupplierShipmentDTO> createSupplierShipmentDTOs = new ArrayList<>();
        for (SupplierShipment shipment : supplierShipments) {
            CreateSupplierShipmentDTO createSupplierShipmentDTO = getCreateSupplierShipmentDTO(shipment);

            createSupplierShipmentDTOs.add(createSupplierShipmentDTO);
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        supplierShipmentsWriteService.createSupplierShipmentsInBulk(createSupplierShipmentDTOs)
                .thenApply(this::handleCreateSupplierShipmentsResponse)
                .exceptionally(this::handleCreateSupplierShipmentsException);
    }

    private CreateSupplierShipmentDTO getCreateSupplierShipmentDTO(SupplierShipment shipment) {
        CreateSupplierShipmentDTO createSupplierShipmentDTO = new CreateSupplierShipmentDTO();
        if (supplier == null || supplier.getOrganizationId() == null) {
            throw new IllegalArgumentException("Supplier Organization ID is missing");
        }
        createSupplierShipmentDTO.setOrganizationId(supplier.getOrganizationId());
        createSupplierShipmentDTO.setSupplierId(supplier.getId());
        createSupplierShipmentDTO.setSupplierOrderId(shipment.getSupplierOrderId());
        createSupplierShipmentDTO.setComponentId(shipment.getComponentId());
        createSupplierShipmentDTO.setQuantity(shipment.getQuantity());
        createSupplierShipmentDTO.setDeliveredQuantity(shipment.getDeliveredQuantity());
        createSupplierShipmentDTO.setArrivalDate(shipment.getArrivalDate());
        createSupplierShipmentDTO.setEstimatedArrivalDate(shipment.getEstimatedArrivalDate());
        createSupplierShipmentDTO.setShipmentStartingDate(shipment.getShipmentStartingDate());
        createSupplierShipmentDTO.setStatus(shipment.getStatus());

        createSupplierShipmentDTO.setCompanyId(shipment.getCompanyId());
        return createSupplierShipmentDTO;
    }

    private Result<List<SupplierShipment>> handleCreateSupplierShipmentsResponse(Result<List<SupplierShipment>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the Supplier Shipments.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            isNewShipmentMode.set(false);
            closeConfirmCreateDialog();
            updateUIOnSuccessfulOperation();
            ToastInfo toastInfo = new ToastInfo("Success", "Supplier Shipments created successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<SupplierShipment>> handleCreateSupplierShipmentsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error creating the Supplier Shipments.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void handleUpdateShipments(List<SupplierShipment> supplierShipments) {
        List<UpdateSupplierShipmentDTO> updateSupplierShipmentDTOs = new ArrayList<>();

        for (SupplierShipment shipment : supplierShipments) {
            UpdateSupplierShipmentDTO updateSupplierShipmentDTO = getUpdateSupplierShipmentDTO(shipment);

            updateSupplierShipmentDTOs.add(updateSupplierShipmentDTO);
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        supplierShipmentsWriteService.updateSupplierShipmentsInBulk(updateSupplierShipmentDTOs)
                .thenApply(this::handleUpdateSupplierShipmentsResponse)
                .exceptionally(this::handleUpdateSupplierShipmentsException);
    }

    private UpdateSupplierShipmentDTO getUpdateSupplierShipmentDTO(SupplierShipment shipment) {
        UpdateSupplierShipmentDTO updateSupplierShipmentDTO = new UpdateSupplierShipmentDTO();
        updateSupplierShipmentDTO.setId(shipment.getId());
        updateSupplierShipmentDTO.setOrganizationId(shipment.getOrganizationId());
        updateSupplierShipmentDTO.setSupplierOrderId(shipment.getSupplierOrderId());
        updateSupplierShipmentDTO.setQuantity(shipment.getQuantity());
        updateSupplierShipmentDTO.setShipmentStartingDate(shipment.getShipmentStartingDate());
        updateSupplierShipmentDTO.setEstimatedArrivalDate(shipment.getEstimatedArrivalDate());
        updateSupplierShipmentDTO.setArrivalDate(shipment.getArrivalDate());
        updateSupplierShipmentDTO.setStatus(shipment.getStatus());
        updateSupplierShipmentDTO.setSourceLocationId(shipment.getSourceLocation().getId());
        updateSupplierShipmentDTO.setDestinationLocationId(shipment.getDestinationLocation().getId());
        updateSupplierShipmentDTO.setCurrentLocationLatitude(shipment.getCurrentLocationLatitude());
        updateSupplierShipmentDTO.setCurrentLocationLongitude(shipment.getCurrentLocationLongitude());

        return updateSupplierShipmentDTO;
    }

    private Result<List<SupplierShipment>> handleUpdateSupplierShipmentsResponse(Result<List<SupplierShipment>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error updating the Supplier Shipments.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            isNewShipmentMode.set(false);
            closeConfirmUpdateDialog();
            updateUIOnSuccessfulOperation();
            ToastInfo toastInfo = new ToastInfo("Success", "Supplier Shipments updated successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<SupplierShipment>> handleUpdateSupplierShipmentsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error updating the Supplier Shipments.", OperationOutcome.ERROR);
            toastManager.addToast(toastInfo);
        });
        return new Result<>();
    }

    private void handleDeleteShipments(List<SupplierShipment> supplierShipments) {
        List<Integer> shipmentsToRemoveIds = new ArrayList<>();
        for (SupplierShipment shipment : supplierShipments) {
            shipmentsToRemoveIds.add(shipment.getId());
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        supplierShipmentsWriteService.deleteSupplierShipmentInBulk(shipmentsToRemoveIds)
                .thenApply(this::handleDeleteSupplierShipmentsResponse)
                .exceptionally(this::handleDeleteSupplierShipmentsException);
    }

    private Result<List<Integer>> handleDeleteSupplierShipmentsResponse(Result<List<Integer>> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                ToastInfo toastInfo = new ToastInfo("Error", "There was an error deleting the Supplier Shipments.", OperationOutcome.ERROR);
                toastManager.addToast(toastInfo);
                return;
            }

            tableView.getItems().removeIf(tableData -> result.getData().contains(tableData.getData().getId()));

            closeConfirmDeleteDialog();
            isEditMode.set(false);
            tableView.refresh();
            selectedRowsIndices.clear();
            ToastInfo toastInfo = new ToastInfo("Success", "Supplier Shipments deleted successfully.", OperationOutcome.SUCCESS);
            toastManager.addToast(toastInfo);
        });
        return  result;
    }

    private Result<List<Integer>> handleDeleteSupplierShipmentsException(Throwable throwable) {
        Platform.runLater(() -> {
            ToastInfo toastInfo = new ToastInfo("Error", "There was an error deleting the Supplier Shipments.", OperationOutcome.ERROR);
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
            TableData<SupplierShipment> tableRow = tableView.getItems().get(rowIndex);
            tableRow.setSelected(false);
        }

        selectRowColumn.setEditable(true);
        selectedRowsIndices.clear();
        tableView.refresh();
    }
}