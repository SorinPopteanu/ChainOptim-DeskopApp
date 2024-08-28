package org.chainoptim.desktop.features.storage.warehouse.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.storage.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.storage.warehouse.service.WarehouseService;
import org.chainoptim.desktop.features.storage.warehouse.service.WarehouseWriteService;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class WarehouseController implements Initializable {

    // Services
    private final WarehouseService warehouseService;
    private final WarehouseWriteService warehouseWriteService;
    private final CommonViewsLoader commonViewsLoader;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;

    // Controllers
    private GenericConfirmDialogController<Warehouse> confirmWarehouseDeleteController;

    // Listeners
    private RunnableConfirmDialogActionListener<Warehouse> confirmDialogDeleteListener;

    // State
    private final FallbackManager fallbackManager;
    private final ToastManager toastManager;
    private Warehouse warehouse;

    // FXML
    @FXML
    private Label warehouseName;
    @FXML
    private Label warehouseLocation;
    @FXML
    private Button deleteButton;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab overviewTab;
    @FXML
    private Tab inventoryTab;
    @FXML
    private Tab storageTab;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane confirmDeleteDialogContainer;

    @Inject
    public WarehouseController(WarehouseService warehouseService,
                               WarehouseWriteService warehouseWriteService,
                               CommonViewsLoader commonViewsLoader,
                               NavigationService navigationService,
                               CurrentSelectionService currentSelectionService,
                               FallbackManager fallbackManager,
                               ToastManager toastManager) {
        this.warehouseService = warehouseService;
        this.warehouseWriteService = warehouseWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
        this.toastManager = toastManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setUpListeners();
        loadDeleteButton();
        loadComponents();

        Integer warehouseId = currentSelectionService.getSelectedId();
        if (warehouseId != null) {
            loadWarehouse(warehouseId);
        } else {
            fallbackManager.setErrorMessage("Failed to load warehouse.");
        }
    }

    private void setUpListeners() {
        setUpFallbackListeners();
        setUpTabListeners();
        setUpDialogListeners();
    }

    private void setUpFallbackListeners() {
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            tabPane.setVisible(newValue);
            tabPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void setUpTabListeners() {
        overviewTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && overviewTab.getContent() == null) {
                commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/storage/WarehouseOverviewView.fxml", this.warehouse);
            }
        });
        inventoryTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && inventoryTab.getContent() == null) {
                commonViewsLoader.loadTabContent(inventoryTab, "/org/chainoptim/desktop/features/storage/WarehouseInventoryView.fxml", new SearchData<>(this.warehouse, SearchMode.SECONDARY));
            }
        });
        storageTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && storageTab.getContent() == null) {
                commonViewsLoader.loadTabContent(storageTab, "/org/chainoptim/desktop/features/storage/WarehouseStorageView.fxml", this.warehouse);
            }
        });
    }

    private void setUpDialogListeners() {
        Consumer<Warehouse> onConfirmDelete = this::handleDeleteWarehouse;
        Runnable onCancelDelete = this::closeConfirmDeleteDialog;
        confirmDialogDeleteListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);
    }

    // Loading
    private void loadDeleteButton() {
        Image deleteImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/trash-solid.png")));
        ImageView deleteImageView = new ImageView(deleteImage);
        deleteImageView.setFitWidth(14);
        deleteImageView.setFitHeight(14);
        deleteButton.setGraphic(deleteImageView);
        deleteButton.setTooltip(new Tooltip("Delete warehouse"));
        deleteButton.setOnAction(event -> openConfirmDeleteDialog(warehouse));
    }

    private void loadComponents() {
        confirmWarehouseDeleteController = commonViewsLoader.loadConfirmDialog(confirmDeleteDialogContainer);
        confirmWarehouseDeleteController.setActionListener(confirmDialogDeleteListener);
        closeConfirmDeleteDialog();
    }


    private void loadWarehouse(Integer warehouseId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        warehouseService.getWarehouseById(warehouseId)
                .thenApply(this::handleWarehouseResponse)
                .exceptionally(this::handleWarehouseException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Result<Warehouse> handleWarehouseResponse(Result<Warehouse> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load warehouse.");
                return;
            }
            this.warehouse = result.getData();
            warehouseName.setText(warehouse.getName());
            warehouseLocation.setText(warehouse.getLocation().getFormattedLocation());

            commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/storage/WarehouseOverviewView.fxml", this.warehouse);
        });
        return result;
    }

    private Result<Warehouse> handleWarehouseException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load warehouse."));
        return new Result<>();
    }

    // Actions
    @FXML
    private void handleEditWarehouse() {
        currentSelectionService.setSelectedId(warehouse.getId());
        navigationService.switchView("Update-Warehouse?id=" + warehouse.getId(), true, null);
    }

    private void openConfirmDeleteDialog(Warehouse warehouse) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Warehouse Delete",
                "Are you sure you want to delete this warehouse? This action cannot be undone.",
                null);
        confirmWarehouseDeleteController.setData(warehouse, confirmDialogInput);
        toggleDialogVisibility(confirmDeleteDialogContainer, true);
    }

    private void handleDeleteWarehouse(Warehouse warehouse) {
        fallbackManager.setLoading(true);

        warehouseWriteService.deleteWarehouse(warehouse.getId())
                .thenApply(this::handleDeleteResponse)
                .exceptionally(this::handleDeleteException);
    }

    private Result<Integer> handleDeleteResponse(Result<Integer> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Failed to delete warehouse.",
                        "An error occurred while deleting the warehouse.",
                        OperationOutcome.ERROR)
                );
                return;
            }

            toastManager.addToast(new ToastInfo(
                    "Warehouse deleted.",
                    "The Warehouse \"" + warehouse.getName() + "\" has been successfully deleted.",
                    OperationOutcome.SUCCESS)
            );

            NavigationServiceImpl.invalidateViewCache("Warehouses");
            navigationService.switchView("Warehouses", true, null);
        });
        return result;
    }

    private Result<Integer> handleDeleteException(Throwable ex) {
        Platform.runLater(() ->
                toastManager.addToast(new ToastInfo(
                        "Failed to delete warehouse.",
                        "An error occurred while deleting the warehouse.",
                        OperationOutcome.ERROR)
                )
        );
        return new Result<>();
    }

    private void closeConfirmDeleteDialog() {
        toggleDialogVisibility(confirmDeleteDialogContainer, false);
    }

    private void toggleDialogVisibility(StackPane dialogContainer, boolean isVisible) {
        dialogContainer.setVisible(isVisible);
        dialogContainer.setManaged(isVisible);
    }
}
