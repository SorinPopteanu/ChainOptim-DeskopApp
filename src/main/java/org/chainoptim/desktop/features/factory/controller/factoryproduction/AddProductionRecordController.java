package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.DailyProductionRecord;
import org.chainoptim.desktop.features.scanalysis.productionhistory.service.FactoryProductionHistoryService;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationResult;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocation;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationPersistenceService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.util.*;

public class AddProductionRecordController implements DataReceiver<Factory> {

    // Services
    private final FactoryProductionHistoryService historyService;
    private final ResourceAllocationPersistenceService allocationPersistenceService;
    private final CommonViewsLoader commonViewsLoader;

    // State
    private final FallbackManager fallbackManager;
    private ResourceAllocationPlan currentPlan;
    private Factory factory;
    private DailyProductionRecord newRecord;
    private Map<Integer, DailyProductionRecord> recordsByStageId;

    @FXML
    private Label currentPlanStartDate;
    @FXML
    private Label currentPlanEndDate;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private VBox allocationsVBox;
    @FXML
    private List<GridPane> stageInputsGridPanes = new ArrayList<>();
    @FXML
    private List<GridPane> stageOutputsGridPanes = new ArrayList<>();


    @Inject
    public AddProductionRecordController(FactoryProductionHistoryService historyService,
                                         ResourceAllocationPersistenceService allocationPersistenceService,
                                         CommonViewsLoader commonViewsLoader,
                                         FallbackManager fallbackManager) {
        this.historyService = historyService;
        this.allocationPersistenceService = allocationPersistenceService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Factory factory) {
        this.factory = factory;
        setUpListeners();
        loadCurrentPlan(factory.getId());
    }

    private void setUpListeners() {
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> rerenderPlan());

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> rerenderPlan());
    }

    private void rerenderPlan() {
        if (currentPlan == null) return;
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) return;

        // Calculate record duration
        long recordDuration = endDatePicker.getValue().toEpochDay() - startDatePicker.getValue().toEpochDay();
        if (recordDuration < 0) return;

        // Create a new plan based on the original plan
        AllocationPlan adjustedPlan = createAdjustedPlan(recordDuration);

        // Display the form with the adjusted plan
        displayFormWithPlan(adjustedPlan);
    }

    private AllocationPlan createAdjustedPlan(long recordDuration) {
        if (currentPlan == null || currentPlan.getAllocationPlan() == null) {
            return null; // Handle the case where the current plan or its allocation plan is null
        }

        AllocationPlan originalPlan = currentPlan.getAllocationPlan(); // Get the original plan

        AllocationPlan adjustedPlan = new AllocationPlan();
        // Copy relevant fields from the original plan
        adjustedPlan.setAllocations(new ArrayList<>(originalPlan.getAllocations()));
        adjustedPlan.setResults(new ArrayList<>(originalPlan.getResults()));
        adjustedPlan.setDurationDays(originalPlan.getDurationDays());

        // Adjust allocations and results for the new duration
        float durationRatio = recordDuration / originalPlan.getDurationDays();
        for (ResourceAllocation allocation : adjustedPlan.getAllocations()) {
            allocation.setRequestedAmount(allocation.getRequestedAmount() * durationRatio);
            allocation.setAllocatedAmount(allocation.getAllocatedAmount() * durationRatio);
        }
        for (AllocationResult result : adjustedPlan.getResults()) {
            result.setFullAmount(result.getFullAmount() * durationRatio);
            result.setResultedAmount(result.getResultedAmount() * durationRatio);
        }

        return adjustedPlan;
    }


    private void loadCurrentPlan(Integer factoryId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        allocationPersistenceService.getResourceAllocationPlanByFactoryId(factoryId)
                .thenApply(this::handleCurrentPlanResponse)
                .exceptionally(this::handleCurrentPlanException);
    }

    private Optional<ResourceAllocationPlan> handleCurrentPlanResponse(Optional<ResourceAllocationPlan> planOptional) {
        Platform.runLater(() -> {
            if (planOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load current allocation plan.");
                return;
            }
            currentPlan = planOptional.get();
            System.out.println("Current Plan: " + currentPlan);
            fallbackManager.setLoading(false);

            currentPlanStartDate.setText(currentPlan.getActivationDate().toLocalDate().toString());
            LocalDateTime planEndDate = currentPlan.getActivationDate().plusDays((long) Math.floor(currentPlan.getAllocationPlan().getDurationDays()));
            currentPlanEndDate.setText(planEndDate.toLocalDate().toString());

            displayFormWithPlan(currentPlan.getAllocationPlan());
        });
        return planOptional;
    }

    private Optional<ResourceAllocationPlan> handleCurrentPlanException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load current allocation plan."));
        return Optional.empty();
    }

    private void displayFormWithPlan(AllocationPlan allocationPlan) {
        allocationsVBox.getChildren().clear();
        allocationsVBox.setSpacing(8);
        stageInputsGridPanes.clear();
        stageOutputsGridPanes.clear();

        groupRecordsByStageId(allocationPlan);

        for (Map.Entry<Integer, DailyProductionRecord> entry : recordsByStageId.entrySet()) {
            Label stageNameLabel = addStageLabel(entry.getValue().getAllocations().getFirst().getStageName());
            allocationsVBox.getChildren().add(stageNameLabel);

            // Stage Inputs
            Label stageInputLabel = new Label("• Stage Inputs");
            stageInputLabel.setStyle("-fx-padding: 8px 0 0 0; -fx-font-size: 16px; -fx-font-weight: bold;");
            allocationsVBox.getChildren().add(stageInputLabel);

            if (entry.getValue().getAllocations() == null) {
                stageInputLabel.setText("Stage Inputs: None");
                continue;
            }

            GridPane stageInputGridPane = new GridPane();
            stageInputGridPane.setHgap(16);
            stageInputGridPane.setVgap(8);
            stageInputGridPane.setPadding(new Insets(8));

            // - Headers
            renderColumnHeader(stageInputGridPane, "Component", 0);
            renderColumnHeader(stageInputGridPane, "Actual Amount", 1);
            renderColumnHeader(stageInputGridPane, "Planned Amount", 2);
            renderColumnHeader(stageInputGridPane, "Needed Amount", 3);

            // - Rows
            for (int i = 0; i < entry.getValue().getAllocations().size(); i++) {
                renderStageInputGridPane(stageInputGridPane, entry.getValue().getAllocations().get(i), i + 1);
            }

            stageInputsGridPanes.add(stageInputGridPane);
            allocationsVBox.getChildren().add(stageInputGridPane);

            // Stage Outputs
            Label stageOutputLabel = new Label("• Stage Outputs");
            stageOutputLabel.setStyle("-fx-padding: 8px 0 0 0; -fx-font-size: 16px; -fx-font-weight: bold;");
            allocationsVBox.getChildren().add(stageOutputLabel);

            if (entry.getValue().getResults() == null) {
                stageOutputLabel.setText("Stage Outputs: None");
                continue;
            }

            GridPane stageOutputGridPane = new GridPane();
            stageOutputGridPane.setHgap(16);
            stageOutputGridPane.setVgap(8);
            stageOutputGridPane.setPadding(new Insets(8));

            // - Headers
            renderColumnHeader(stageOutputGridPane, "Component", 0);
            renderColumnHeader(stageOutputGridPane, "Actual Amount", 1);
            renderColumnHeader(stageOutputGridPane, "Expected Amount", 2);
            renderColumnHeader(stageOutputGridPane, "Full Amount", 3);

            // - Rows
            for (int i = 0; i < entry.getValue().getResults().size(); i++) {
                renderStageOutputGridPane(stageOutputGridPane, entry.getValue().getResults().get(i), i + 1);
            }

            stageOutputsGridPanes.add(stageOutputGridPane);
            allocationsVBox.getChildren().add(stageOutputGridPane);
        }
    }

    private void groupRecordsByStageId(AllocationPlan allocationPlan) {
        recordsByStageId = new HashMap<>();

        List<Integer> uniqueStageIds = allocationPlan.getAllocations().stream()
                .map(ResourceAllocation::getFactoryStageId)
                .distinct()
                .toList();

        for (Integer stageId : uniqueStageIds) {
            DailyProductionRecord stageRecord = new DailyProductionRecord();
            stageRecord.setAllocations(
                    allocationPlan.getAllocations().stream()
                            .filter(allocation -> allocation.getFactoryStageId().equals(stageId))
                            .toList()
            );
            stageRecord.setResults(
                    allocationPlan.getResults().stream()
                            .filter(result -> result.getFactoryStageId().equals(stageId))
                            .toList()
            );
            stageRecord.setDurationDays(allocationPlan.getDurationDays());
            recordsByStageId.put(stageId, stageRecord);
        }
    }

    private Label addStageLabel(String stageName) {
        Label stageNameLabel = new Label("Stage: " + stageName);
        stageNameLabel.getStyleClass().setAll("general-label-large");
        stageNameLabel.setStyle("-fx-padding: 8px 0 0 0;");
        return stageNameLabel;
    }

    private void renderColumnHeader(GridPane stageGridPane, String headerText, int columnIndex) {
        Label headerLabel = new Label(headerText);
        headerLabel.getStyleClass().setAll("general-label");

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(80);
        stageGridPane.getColumnConstraints().add(columnIndex, columnConstraints);

        stageGridPane.add(headerLabel, columnIndex, 0);
    }

    private void renderStageInputGridPane(GridPane stageGridPane, ResourceAllocation allocation, int rowIndex) {
        Label componentNameLabel = new Label(allocation.getComponentName());
        componentNameLabel.getStyleClass().setAll("general-label");
        stageGridPane.add(componentNameLabel, 0, rowIndex);

        TextField actualAmountField = new TextField();
        actualAmountField.getStyleClass().setAll("custom-text-field");
        stageGridPane.add(actualAmountField, 1, rowIndex);

        Label plannedAmountValue = new Label(String.valueOf(allocation.getAllocatedAmount()));
        plannedAmountValue.getStyleClass().setAll("general-label");
        plannedAmountValue.setStyle("-fx-text-fill: #121212");
        stageGridPane.add(plannedAmountValue, 2, rowIndex);

        Label neededAmountValue = new Label(String.valueOf(allocation.getRequestedAmount()));
        neededAmountValue.getStyleClass().setAll("general-label");
        neededAmountValue.setStyle("-fx-text-fill: #121212");
        stageGridPane.add(neededAmountValue, 3, rowIndex);
    }

    private void renderStageOutputGridPane(GridPane stageGridPane, AllocationResult allocationResult, int rowIndex) {
        Label componentNameLabel = new Label(allocationResult.getComponentName());
        componentNameLabel.getStyleClass().setAll("general-label");
        componentNameLabel.setStyle("-fx-padding: 4px;");
        stageGridPane.add(componentNameLabel, 0, rowIndex);

        TextField actualAmountField = new TextField();
        actualAmountField.getStyleClass().setAll("custom-text-field");
        stageGridPane.add(actualAmountField, 1, rowIndex);

        Label expectedAmountValue = new Label(String.valueOf(allocationResult.getResultedAmount()));
        expectedAmountValue.getStyleClass().setAll("general-label");
        expectedAmountValue.setStyle("-fx-text-fill: #121212");
        stageGridPane.add(expectedAmountValue, 2, rowIndex);

        Label fullAmountValue = new Label(String.valueOf(allocationResult.getFullAmount()));
        fullAmountValue.getStyleClass().setAll("general-label");
        fullAmountValue.setStyle("-fx-text-fill: #121212");
        stageGridPane.add(fullAmountValue, 3, rowIndex);
    }
}
