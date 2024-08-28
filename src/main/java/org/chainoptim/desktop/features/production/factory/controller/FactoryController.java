package org.chainoptim.desktop.features.production.factory.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.production.factory.service.FactoryWriteService;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.common.ui.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.common.ui.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.production.factory.model.Factory;
import org.chainoptim.desktop.features.production.factory.service.FactoryService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import com.google.inject.Inject;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class FactoryController implements Initializable {

    // Services
    private final FactoryService factoryService;
    private final FactoryWriteService factoryWriteService;
    private final CommonViewsLoader commonViewsLoader;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;

    // Controllers
    private GenericConfirmDialogController<Factory> confirmFactoryDeleteController;

    // Listeners
    private RunnableConfirmDialogActionListener<Factory> confirmDialogDeleteListener;

    // State
    private final FallbackManager fallbackManager;
    private final ToastManager toastManager;
    private Factory factory;
    private static final String GENERIC_ERROR_MESSAGE = "Failed to load factory.";

    // FXML
    @FXML
    private Label factoryName;
    @FXML
    private Label factoryLocation;
    @FXML
    private Button deleteButton;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab overviewTab;
    @FXML
    private Tab productionTab;
    @FXML
    private Tab inventoryTab;
    @FXML
    private Tab performanceTab;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane confirmDeleteDialogContainer;

    @Inject
    public FactoryController(FactoryService factoryService,
                             FactoryWriteService factoryWriteService,
                             CommonViewsLoader  commonViewsLoader,
                             NavigationService navigationService,
                             CurrentSelectionService currentSelectionService,
                             FallbackManager fallbackManager,
                             ToastManager toastManager) {
        this.factoryService = factoryService;
        this.factoryWriteService = factoryWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
        this.toastManager = toastManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setupListeners();
        loadDeleteButton();
        loadComponents();

        Integer factoryId = currentSelectionService.getSelectedId();
        if (factoryId != null) {
            loadFactory(factoryId);
        } else {
            System.out.println("Missing factory id.");
            fallbackManager.setErrorMessage(GENERIC_ERROR_MESSAGE);
        }
    }

    private void loadFactory(Integer factoryId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryService.getFactoryById(factoryId)
                .thenApply(this::handleFactoryResponse)
                .exceptionally(this::handleFactoryException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Result<Factory> handleFactoryResponse(Result<Factory> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage(GENERIC_ERROR_MESSAGE);
                return;
            }
            this.factory = result.getData();
            factoryName.setText(factory.getName());
            factoryLocation.setText(factory.getLocation().getFormattedLocation());

            // Load overview tab
            commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/production/FactoryOverviewView.fxml", this.factory);
        });
        return result;
    }

    private Result<Factory> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage(GENERIC_ERROR_MESSAGE));
        return new Result<>();
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
                commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/production/FactoryOverviewView.fxml", this.factory);
            }
        });
        productionTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && productionTab.getContent() == null) {
                commonViewsLoader.loadTabContent(productionTab, "/org/chainoptim/desktop/features/production/FactoryProductionView.fxml", this.factory);
            }
        });
        inventoryTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && inventoryTab.getContent() == null) {
                commonViewsLoader.loadTabContent(inventoryTab, "/org/chainoptim/desktop/features/production/FactoryInventoryView.fxml", new SearchData<>(this.factory, SearchMode.SECONDARY));
            }
        });
        performanceTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && performanceTab.getContent() == null) {
                commonViewsLoader.loadTabContent(performanceTab, "/org/chainoptim/desktop/features/production/FactoryPerformanceView.fxml", this.factory);
            }
        });
    }

    private void setUpDialogListeners() {
        Consumer<Factory> onConfirmDelete = this::handleDeleteFactory;
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
        deleteButton.setTooltip(new Tooltip("Delete factory"));
        deleteButton.setOnAction(event -> openConfirmDeleteDialog(factory));
    }

    private void loadComponents() {
        confirmFactoryDeleteController = commonViewsLoader.loadConfirmDialog(confirmDeleteDialogContainer);
        confirmFactoryDeleteController.setActionListener(confirmDialogDeleteListener);
        closeConfirmDeleteDialog();
    }

    // Actions
    @FXML
    private void handleEditFactory() {
        currentSelectionService.setSelectedId(factory.getId());
        navigationService.switchView("Update-Factory?id=" + factory.getId(), true, null);
    }

    private void openConfirmDeleteDialog(Factory factory) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Factory Delete",
                "Are you sure you want to delete this factory? This action cannot be undone.",
                null);
        confirmFactoryDeleteController.setData(factory, confirmDialogInput);
        toggleDialogVisibility(confirmDeleteDialogContainer, true);
    }

    private void handleDeleteFactory(Factory factory) {
        fallbackManager.setLoading(true);

        factoryWriteService.deleteFactory(factory.getId())
                .thenApply(this::handleDeleteResponse)
                .exceptionally(this::handleDeleteException);
    }

    private Result<Integer> handleDeleteResponse(Result<Integer> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Failed to delete factory.",
                        "An error occurred while deleting the factory.",
                        OperationOutcome.ERROR)
                );
                return;
            }

            toastManager.addToast(new ToastInfo(
                    "Factory deleted.",
                    "The Factory \"" + factory.getName() + "\" has been successfully deleted.",
                    OperationOutcome.SUCCESS)
            );

            NavigationServiceImpl.invalidateViewCache("Factories");
            navigationService.switchView("Factories", true, null);
        });
        return result;
    }

    private Result<Integer> handleDeleteException(Throwable ex) {
        Platform.runLater(() ->
                toastManager.addToast(new ToastInfo(
                        "Failed to delete factory.",
                        "An error occurred while deleting the factory.",
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
