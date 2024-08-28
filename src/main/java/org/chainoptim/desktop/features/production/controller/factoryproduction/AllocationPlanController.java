package org.chainoptim.desktop.features.production.controller.factoryproduction;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.dto.UpdateAllocationPlanDTO;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocation;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationPersistenceService;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.TimeUtil;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;

public class AllocationPlanController {

    // Services
    private final ResourceAllocationPersistenceService resourceAllocationPersistenceService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    // State
    private final FallbackManager fallbackManager;
    private Integer factoryId;
    private ResourceAllocationPlan currentPlan;
    private AllocationPlan allocationPlan;
    private boolean isCurrentPlan;
    private boolean isPlanActive;

    // Listeners
    private RunnableConfirmDialogActionListener<AllocationPlan> confirmDialogActivatePlanListener;
    private RunnableConfirmDialogActionListener<AllocationPlan> confirmDialogDeactivatePlanListener;
    private RunnableConfirmDialogActionListener<AllocationPlan> confirmDialogReplacePlanListener;

    // FXML
    @FXML
    private Label durationStartDateLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Button deactivatePlanButton;
    @FXML
    private Button replaceCurrentPlanButton;
    @FXML
    private Button activatePlanButton;
    @FXML
    private TableView<ResourceAllocation> tableView;
    @FXML
    private TableColumn<ResourceAllocation, String> componentNameColumn;
    @FXML
    private TableColumn<ResourceAllocation, Float> allocatedAmountColumn;
    @FXML
    private TableColumn<ResourceAllocation, Float> requestedAmountColumn;
    @FXML
    private TableColumn<ResourceAllocation, Float> deficitColumn;
    @FXML
    private TableColumn<ResourceAllocation, String> deficitPercentageColumn;
    @FXML
    private StackPane activateConfirmDialogPane;
    @FXML
    private StackPane deactivateConfirmDialogPane;
    @FXML
    private StackPane replaceConfirmDialogPane;

    // Icons
    private Image saveImage;

    @Inject
    public AllocationPlanController(ResourceAllocationPersistenceService resourceAllocationPersistenceService,
                                    FXMLLoaderService fxmlLoaderService,
                                    ControllerFactory controllerFactory,
                                    FallbackManager fallbackManager) {
        this.resourceAllocationPersistenceService = resourceAllocationPersistenceService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    public void initialize(AllocationPlan allocationPlan, Integer factoryId, boolean isCurrentPlan) {
        initializeIcons();
        setUpListeners();

        this.factoryId = factoryId;
        this.isCurrentPlan = isCurrentPlan;

        styleActivatePlanButton(activatePlanButton);
        styleDeactivatePlan(deactivatePlanButton);
        styleReplaceCurrentPlanButton(replaceCurrentPlanButton);

        if (!isCurrentPlan) {
            this.allocationPlan = allocationPlan;
            displayAllocations();
        }
        loadCurrentPlan(factoryId);
    }

    private void initializeIcons() {
        saveImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/floppy-disk-solid.png")));
    }

    private void setUpListeners() {
        Consumer<AllocationPlan> onConfirmActivatePlan = this::activatePlan;
        Runnable onCancelActivation = this::cancelActivation;
        confirmDialogActivatePlanListener = new RunnableConfirmDialogActionListener<>(onConfirmActivatePlan, onCancelActivation);

        Consumer<AllocationPlan> onConfirmDeactivatePlan = this::deactivatePlan;
        Runnable onCancelDeactivation = this::cancelActivation;
        confirmDialogDeactivatePlanListener = new RunnableConfirmDialogActionListener<>(onConfirmDeactivatePlan, onCancelDeactivation);

        Consumer<AllocationPlan> onConfirmReplacePlan = this::replaceCurrentPlan;
        Runnable onCancelReplacement = this::cancelActivation;
        confirmDialogReplacePlanListener = new RunnableConfirmDialogActionListener<>(onConfirmReplacePlan, onCancelReplacement);
    }

    private void setUpConfirmDialogs() {
        ConfirmDialogInput activateDialog = new ConfirmDialogInput("Activate Plan", "Are you sure you want to activate this plan?", null);
        loadConfirmDialog(activateDialog, activateConfirmDialogPane, confirmDialogActivatePlanListener, allocationPlan);

        ConfirmDialogInput deactivateDialog = new ConfirmDialogInput("Deactivate Plan", "Are you sure you want to deactivate this plan?", null);
        loadConfirmDialog(deactivateDialog, deactivateConfirmDialogPane, confirmDialogDeactivatePlanListener, allocationPlan);

        ConfirmDialogInput replaceDialog = new ConfirmDialogInput("Replace Current Plan", "Are you sure you want to replace the current plan?", null);
        loadConfirmDialog(replaceDialog, replaceConfirmDialogPane, confirmDialogReplacePlanListener, allocationPlan);
    }

    private void loadConfirmDialog(ConfirmDialogInput confirmDialogInput,
                                   StackPane confirmDialogPane,
                                   RunnableConfirmDialogActionListener<AllocationPlan> listener,
                                   AllocationPlan allocationPlan) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader("/org/chainoptim/desktop/shared/common/uielements/confirmdialog/GenericConfirmDialogView.fxml", controllerFactory::createController);

        try {
            Node view = loader.load();
            GenericConfirmDialogController<AllocationPlan> controller = loader.getController();
            controller.setData(allocationPlan, confirmDialogInput);
            controller.setActionListener(listener);
            confirmDialogPane.getChildren().add(view);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadCurrentPlan(Integer factoryId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        resourceAllocationPersistenceService.getResourceAllocationPlanByFactoryId(factoryId)
                .thenApply(this::handleCurrentPlanResponse)
                .exceptionally(this::handleCurrentPlanException);
    }

    private Result<ResourceAllocationPlan> handleCurrentPlanResponse(Result<ResourceAllocationPlan> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load current allocation plan.");
                return;
            }
            currentPlan = result.getData();
            fallbackManager.setLoading(false);

            setUpConfirmDialogs();

            if (currentPlan.getAllocationPlan() == null) {
                isPlanActive = false;
                adjustButtonsVisibilityBasedOnPlans();
                return;
            }

            isPlanActive = true;
            adjustButtonsVisibilityBasedOnPlans();

            if (isCurrentPlan) {
                allocationPlan = currentPlan.getAllocationPlan();
                displayAllocations();
            }

//            addDurationLabels(planOptional.get().getAllocationPlan().getDurationDays(), planOptional.get().getActivationDate());
        });
        return result;
    }

    private Result<ResourceAllocationPlan> handleCurrentPlanException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load current allocation plan."));
        return new Result<>();
    }

    private void displayAllocations() {
        componentNameColumn.setCellValueFactory(new PropertyValueFactory<>("componentName"));
        allocatedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("allocatedAmount"));
        requestedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("requestedAmount"));
        deficitColumn.setCellValueFactory(cellData -> {
            ResourceAllocation allocation = cellData.getValue();
            float deficit = allocation.getRequestedAmount() - allocation.getAllocatedAmount();
            return new SimpleFloatProperty(deficit).asObject();
        });
        deficitPercentageColumn.setCellValueFactory(cellData -> {
            ResourceAllocation allocation = cellData.getValue();
            float requestedAmount = allocation.getRequestedAmount() != 0 ? allocation.getRequestedAmount() : 1;
            float deficit = allocation.getRequestedAmount() - allocation.getAllocatedAmount();
            float ratio = deficit / requestedAmount;
            return new SimpleStringProperty(ratio * 100 + "%");
        });

        // Change the style of the deficit column based on the ratio of the deficit to the requested amount
        setupDeficitStyling(deficitColumn, false);
        setupDeficitStyling(deficitPercentageColumn, true);

        tableView.getItems().setAll(allocationPlan.getAllocations());
    }


    private <T> void setupDeficitStyling(TableColumn<ResourceAllocation, T> column, boolean isPercentage) {
        column.setCellFactory(col -> new TableCell<ResourceAllocation, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    getStyleClass().removeAll("good-label", "average-label", "bad-label");
                    return;
                }

                ResourceAllocation allocation = getTableView().getItems().get(getIndex());
                float requestedAmount = allocation.getRequestedAmount();
                float allocatedAmount = allocation.getAllocatedAmount();
                float deficit = requestedAmount - allocatedAmount;
                float ratioOrPercentage = isPercentage ? (deficit / requestedAmount) * 100 : deficit / requestedAmount;

                // Setting text based on whether it's a percentage or a ratio
                setText(isPercentage ? String.format("%.2f%%", ratioOrPercentage) : String.format("%.2f", deficit));

                // Remove all previous styles
                getStyleClass().removeAll("good-label", "average-label", "bad-label");

                // Apply new style based on the ratio or percentage
                if (ratioOrPercentage < (isPercentage ? 10 : 0.1)) { // Adjusted thresholds for ratio vs. percentage
                    getStyleClass().add("good-label");
                } else if (ratioOrPercentage < (isPercentage ? 20 : 0.2)) {
                    getStyleClass().add("average-label");
                } else {
                    getStyleClass().add("bad-label");
                }
            }
        });
    }

    private void activatePlan(AllocationPlan plan) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        UpdateAllocationPlanDTO updateAllocationPlanDTO = new UpdateAllocationPlanDTO();
        updateAllocationPlanDTO.setId(currentPlan.getId());
        updateAllocationPlanDTO.setFactoryId(factoryId);
        updateAllocationPlanDTO.setAllocationPlan(plan);
        updateAllocationPlanDTO.setActive(true);
        updateAllocationPlanDTO.setActivationDate(LocalDateTime.now());

        resourceAllocationPersistenceService.updateAllocationPlan(updateAllocationPlanDTO)
                .thenApply(this::handleActivatePlanResponse)
                .exceptionally(this::handleActivatePlanException);
    }

    private Result<ResourceAllocationPlan> handleActivatePlanResponse(Result<ResourceAllocationPlan> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to activate the plan.");
                return;
            }
            fallbackManager.setLoading(false);
            cancelActivation();

            isCurrentPlan = true;
            isPlanActive = true;
            adjustButtonsVisibilityBasedOnPlans();

            allocationPlan = result.getData().getAllocationPlan();
            displayAllocations();
        });
        return result;
    }

    private Result<ResourceAllocationPlan> handleActivatePlanException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to activate the plan."));
        return new Result<>();
    }

    private void cancelActivation() {
        activateConfirmDialogPane.setVisible(false);
        deactivateConfirmDialogPane.setVisible(false);
        replaceConfirmDialogPane.setVisible(false);
    }

    private void deactivatePlan(AllocationPlan plan) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        UpdateAllocationPlanDTO updateAllocationPlanDTO = new UpdateAllocationPlanDTO();
        updateAllocationPlanDTO.setId(currentPlan.getId());
        updateAllocationPlanDTO.setFactoryId(factoryId);
        updateAllocationPlanDTO.setAllocationPlan(null);
        updateAllocationPlanDTO.setActive(false);

        resourceAllocationPersistenceService.updateAllocationPlan(updateAllocationPlanDTO)
                .thenApply(this::handleDeactivatePlanResponse)
                .exceptionally(this::handleDeactivatePlanException);
    }

    private Result<ResourceAllocationPlan> handleDeactivatePlanResponse(Result<ResourceAllocationPlan> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to deactivate the plan.");
                return;
            }
            fallbackManager.setLoading(false);
            cancelActivation();

            isCurrentPlan = false;
            isPlanActive = false;
            adjustButtonsVisibilityBasedOnPlans();

            allocationPlan = null;
            tableView.getItems().clear();
        });
        return result;
    }

    private Result<ResourceAllocationPlan> handleDeactivatePlanException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to deactivate the plan."));
        return new Result<>();
    }

    private void replaceCurrentPlan(AllocationPlan plan) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        UpdateAllocationPlanDTO updateAllocationPlanDTO = new UpdateAllocationPlanDTO();
        updateAllocationPlanDTO.setId(currentPlan.getId());
        updateAllocationPlanDTO.setFactoryId(factoryId);
        updateAllocationPlanDTO.setAllocationPlan(plan);
        updateAllocationPlanDTO.setActive(true);
        updateAllocationPlanDTO.setActivationDate(LocalDateTime.now());

        resourceAllocationPersistenceService.updateAllocationPlan(updateAllocationPlanDTO)
                .thenApply(this::handleReplacePlanResponse)
                .exceptionally(this::handleReplacePlanException);
    }

    private Result<ResourceAllocationPlan> handleReplacePlanResponse(Result<ResourceAllocationPlan> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to replace the plan.");
                return;
            }
            fallbackManager.setLoading(false);
            cancelActivation();

            isCurrentPlan = true;
            isPlanActive = true;
            adjustButtonsVisibilityBasedOnPlans();

            allocationPlan = result.getData().getAllocationPlan();
            displayAllocations();
        });
        return result;
    }

    private Result<ResourceAllocationPlan> handleReplacePlanException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to replace the plan."));
        return new Result<>();
    }


    private void addDurationLabels(Float durationDays, LocalDateTime activationDate) {
        durationLabel.setText(TimeUtil.formatDuration(durationDays));
        durationStartDateLabel.setText(activationDate.toString());
    }

    private void adjustButtonsVisibilityBasedOnPlans() {
        if (isCurrentPlan) {
            adjustButtonsVisibility(isPlanActive, !isPlanActive, false);
        } else {
            adjustButtonsVisibility(false, !isPlanActive, isPlanActive);
        }
    }

    private void adjustButtonsVisibility(boolean isDeactivateVisible, boolean isActivateVisible, boolean isReplaceVisible) {
        adjustButtonVisibility(deactivatePlanButton, isDeactivateVisible);
        adjustButtonVisibility(activatePlanButton, isActivateVisible);
        adjustButtonVisibility(replaceCurrentPlanButton, isReplaceVisible);
    }

    private void styleActivatePlanButton(Button button) {
        button.setText("Activate Plan");
        button.getStyleClass().add("standard-write-button");
        adjustButtonVisibility(button, !isCurrentPlan);
        button.setOnAction(event -> activateConfirmDialogPane.setVisible(true));
    }

    private void styleReplaceCurrentPlanButton(Button button) {
        button.setText("Replace Current Plan");
        button.getStyleClass().add("standard-write-button");
        adjustButtonVisibility(button, !isCurrentPlan);
        button.setOnAction(event -> replaceConfirmDialogPane.setVisible(true));
    }

    private void styleDeactivatePlan(Button button) {
        button.setText("Deactivate Plan");
        button.getStyleClass().add("standard-write-button");
        adjustButtonVisibility(button, isCurrentPlan);
        button.setOnAction(event -> deactivateConfirmDialogPane.setVisible(true));
    }

    private void adjustButtonVisibility(Button button, boolean isVisible) {
        button.setVisible(isVisible);
        button.setManaged(isVisible);
    }
}
