package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocation;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationPersistenceService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;
import java.util.Optional;

public class AllocationPlanController {

    // Services
    private final ResourceAllocationPersistenceService resourceAllocationPersistenceService;

    // State
    private final FallbackManager fallbackManager;
    private AllocationPlan allocationPlan;

    // FXML
    @FXML
    private Button deactivatePlanButton;
    @FXML
    private Button replaceCurrentPlanButton;
    @FXML
    private Button activatePlanButton;
    @FXML
    private TableView<ResourceAllocation> tableView;
    @FXML
    private TableColumn<ResourceAllocation, Float> allocatedAmountColumn;
    @FXML
    private TableColumn<ResourceAllocation, Float> requestedAmountColumn;
    @FXML
    private TableColumn<ResourceAllocation, Float> deficitColumn;
    @FXML
    private TableColumn<ResourceAllocation, String> deficitPercentageColumn;
    @FXML
    private TableColumn<ResourceAllocation, String> componentNameColumn;

    // Icons
    private Image saveImage;

    @Inject
    public AllocationPlanController(ResourceAllocationPersistenceService resourceAllocationPersistenceService,
                                    FallbackManager fallbackManager) {
        this.resourceAllocationPersistenceService = resourceAllocationPersistenceService;
        this.fallbackManager = fallbackManager;
    }

    public void initialize(AllocationPlan allocationPlan, Integer factoryId, boolean isCurrentPlan) {
        initializeIcons();
        styleActivatePlanButton(activatePlanButton, isCurrentPlan);
        styleDeactivatePlan(deactivatePlanButton, isCurrentPlan);

        if (!isCurrentPlan) {
            this.allocationPlan = allocationPlan;
            displayAllocations();
        } else {
            loadCurrentPlan(factoryId);
        }
    }

    private void initializeIcons() {
        saveImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/floppy-disk-solid.png")));
    }

    private void loadCurrentPlan(Integer factoryId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        resourceAllocationPersistenceService.getResourceAllocationPlanByFactoryId(factoryId)
                .thenApply(this::handleCurrentPlanResponse)
                .exceptionally(this::handleCurrentPlanException);
    }

    private Optional<ResourceAllocationPlan> handleCurrentPlanResponse(Optional<ResourceAllocationPlan> planOptional) {
        Platform.runLater(() -> {
            if (planOptional.isEmpty()) {
                fallbackManager.setErrorMessage("No current allocation plan found.");
                return;
            }
            allocationPlan = planOptional.get().getAllocationPlan();
            System.out.println("Current allocation plan: " + allocationPlan);
            fallbackManager.setLoading(false);

            displayAllocations();
        });
        return planOptional;
    }

    private Optional<ResourceAllocationPlan> handleCurrentPlanException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load current allocation plan."));
        return Optional.empty();
    }

    private void displayAllocations() {
        allocatedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("allocatedAmount"));
        requestedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("requestedAmount"));
        deficitColumn.setCellValueFactory(cellData -> {
            ResourceAllocation allocation = cellData.getValue();
            float deficit = allocation.getRequestedAmount() - allocation.getAllocatedAmount();
            return new SimpleFloatProperty(deficit).asObject();
        });
        deficitPercentageColumn.setCellValueFactory(cellData -> {
            ResourceAllocation allocation = cellData.getValue();
            float requestedAmount = allocation.getRequestedAmount();
            float deficit = allocation.getRequestedAmount() - allocation.getAllocatedAmount();
            float ratio = deficit / requestedAmount;
            return new SimpleStringProperty(ratio * 100 + "%");
        });
        componentNameColumn.setCellValueFactory(new PropertyValueFactory<>("componentName"));

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

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }

    private void styleActivatePlanButton(Button button, Boolean isCurrentPlan) {
        button.setText("Activate Plan");
        button.getStyleClass().add("standard-write-button");
        button.setVisible(!isCurrentPlan);
        button.setManaged(!isCurrentPlan);
    }

    private void styleDeactivatePlan(Button button, Boolean isCurrentPlan) {
        button.setText("Deactivate");
        button.getStyleClass().add("standard-write-button");
        button.setVisible(isCurrentPlan);
        button.setManaged(isCurrentPlan);
    }
}
