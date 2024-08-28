package org.chainoptim.desktop.features.supply.supplier.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.supply.supplier.model.Supplier;
import org.chainoptim.desktop.features.supply.supplier.service.SupplierWriteService;
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
import org.chainoptim.desktop.features.supply.supplier.service.SupplierService;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SupplierController implements Initializable {

    // Services
    private final SupplierService supplierService;
    private final SupplierWriteService supplierWriteService;
    private final CommonViewsLoader commonViewsLoader;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;

    // Controllers
    private GenericConfirmDialogController<Supplier> confirmSupplierDeleteController;

    // Listeners
    private RunnableConfirmDialogActionListener<Supplier> confirmDialogDeleteListener;

    // State
    private final FallbackManager fallbackManager;
    private final ToastManager toastManager;
    private Supplier supplier;

    // FXML
    @FXML
    private Label supplierName;
    @FXML
    private Label supplierLocation;
    @FXML
    private Button deleteButton;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab overviewTab;
    @FXML
    private Tab ordersTab;
    @FXML
    private Tab shipmentsTab;
    @FXML
    private Tab performanceTab;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane confirmDeleteDialogContainer;

    @Inject
    public SupplierController(SupplierService supplierService,
                              SupplierWriteService supplierWriteService,
                              CommonViewsLoader commonViewsLoader,
                              NavigationService navigationService,
                              CurrentSelectionService currentSelectionService,
                              FallbackManager fallbackManager,
                              ToastManager toastManager) {
        this.supplierService = supplierService;
        this.supplierWriteService = supplierWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
        this.toastManager = toastManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListeners();
        loadDeleteButton();
        loadComponents();

        Integer supplierId = currentSelectionService.getSelectedId();
        if (supplierId != null) {
            loadSupplier(supplierId);
        } else {
            System.out.println("Missing supplier id.");
            fallbackManager.setErrorMessage("Failed to load supplier.");
        }
    }

    private void setupListeners() {
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
                commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/supply/SupplierOverviewView.fxml", this.supplier);
            }
        });
        ordersTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && ordersTab.getContent() == null) {
                commonViewsLoader.loadTabContent(ordersTab, "/org/chainoptim/desktop/features/supply/SupplierOrdersView.fxml", new SearchData<>(this.supplier, SearchMode.SECONDARY));
            }
        });
        shipmentsTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && shipmentsTab.getContent() == null) {
                commonViewsLoader.loadTabContent(shipmentsTab, "/org/chainoptim/desktop/features/supply/SupplierShipmentsView.fxml", new SearchData<>(this.supplier, SearchMode.SECONDARY));
            }
        });
        performanceTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && performanceTab.getContent() == null) {
                commonViewsLoader.loadTabContent(performanceTab, "/org/chainoptim/desktop/features/supply/SupplierPerformanceView.fxml", new SearchData<>(this.supplier, SearchMode.SECONDARY));
            }
        });
    }
    
    private void setUpDialogListeners() {
        Consumer<Supplier> onConfirmDelete = this::handleDeleteSupplier;
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
        deleteButton.setTooltip(new Tooltip("Delete supplier"));
        deleteButton.setOnAction(event -> openConfirmDeleteDialog(supplier));
    }

    private void loadComponents() {
        confirmSupplierDeleteController = commonViewsLoader.loadConfirmDialog(confirmDeleteDialogContainer);
        confirmSupplierDeleteController.setActionListener(confirmDialogDeleteListener);
        closeConfirmDeleteDialog();
    }


    private void loadSupplier(Integer supplierId) {
        fallbackManager.setLoading(true);

        supplierService.getSupplierById(supplierId)
                .thenApply(this::handleSupplierResponse)
                .exceptionally(this::handleSupplierException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Result<Supplier> handleSupplierResponse(Result<Supplier> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load supplier.");
                return;
            }
            this.supplier = result.getData();
            supplierName.setText(supplier.getName());

            if (supplier.getLocation() != null) {
                supplierLocation.setText(supplier.getLocation().getFormattedLocation());
            } else {
                supplierLocation.setText("");
            }

            commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/supply/SupplierOverviewView.fxml", this.supplier);
        });
        return result;
    }

    private Result<Supplier> handleSupplierException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supplier."));
        return new Result<>();
    }

    // Actions
    @FXML
    private void handleEditSupplier() {
        currentSelectionService.setSelectedId(supplier.getId());
        navigationService.switchView("Update-Supplier?id=" + supplier.getId(), true, null);
    }

    private void openConfirmDeleteDialog(Supplier supplier) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Supplier Delete",
                "Are you sure you want to delete this supplier? This action cannot be undone.",
                null);
        confirmSupplierDeleteController.setData(supplier, confirmDialogInput);
        toggleDialogVisibility(confirmDeleteDialogContainer, true);
    }

    private void handleDeleteSupplier(Supplier supplier) {
        fallbackManager.setLoading(true);

        supplierWriteService.deleteSupplier(supplier.getId())
                .thenApply(this::handleDeleteResponse)
                .exceptionally(this::handleDeleteException);
    }

    private Result<Integer> handleDeleteResponse(Result<Integer> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Failed to delete supplier.",
                        "An error occurred while deleting the supplier.",
                        OperationOutcome.ERROR)
                );
                return;
            }

            toastManager.addToast(new ToastInfo(
                    "Supplier deleted.",
                    "The Supplier \"" + supplier.getName() + "\" has been successfully deleted.",
                    OperationOutcome.SUCCESS)
            );

            NavigationServiceImpl.invalidateViewCache("Suppliers");
            navigationService.switchView("Suppliers", true, null);
        });
        return result;
    }

    private Result<Integer> handleDeleteException(Throwable ex) {
        Platform.runLater(() ->
                toastManager.addToast(new ToastInfo(
                        "Failed to delete supplier.",
                        "An error occurred while deleting the supplier.",
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
