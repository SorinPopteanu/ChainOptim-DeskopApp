package org.chainoptim.desktop.features.production.controller.factoryproduction;

import org.chainoptim.desktop.features.production.model.Factory;
import org.chainoptim.desktop.features.production.model.TabsActionListener;
import org.chainoptim.desktop.features.scanalysis.productionhistory.dto.AddDayToFactoryProductionHistoryDTO;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.DailyProductionRecord;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.FactoryProductionHistory;
import org.chainoptim.desktop.features.scanalysis.productionhistory.service.FactoryProductionHistoryService;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationResult;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocation;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationPersistenceService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

public class AddProductionRecordController implements DataReceiver<Factory> {

    // Services
    private final FactoryProductionHistoryService historyService;
    private final ResourceAllocationPersistenceService allocationPersistenceService;

    // Listeners
    @Setter
    private TabsActionListener actionListener;

    // State
    private final FallbackManager fallbackManager;
    private ResourceAllocationPlan currentPlan;
    private Factory factory;
    private Map<Integer, DailyProductionRecord> recordsByStageId; // Key: Stage ID
    private float previousDuration = 1;
    private final Map<Integer, Integer> layoutIndexToStageId = new HashMap<>(); // Key: Index in stageInputs(/Outputs)GridPanes, Value: Stage ID
    private FactoryProductionHistory productionHistory;

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
                                         FallbackManager fallbackManager) {
        this.historyService = historyService;
        this.allocationPersistenceService = allocationPersistenceService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Factory factory) {
        this.factory = factory;
        setUpListeners();
        loadCurrentPlan(factory.getId());
        loadHistoryId(factory.getId());
    }

    private void setUpListeners() {
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> adjustPlanForDuration());

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> adjustPlanForDuration());
    }

    private void adjustPlanForDuration() {
        if (currentPlan == null) return;
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) return;

        // Calculate record duration
        long recordDuration = endDatePicker.getValue().toEpochDay() - startDatePicker.getValue().toEpochDay();
        if (recordDuration <= 0) return;

        // Adjust the plan for the new duration
        float newDuration = recordDuration / previousDuration;
        if (previousDuration != 1) {
            newDuration *= currentPlan.getAllocationPlan().getDurationDays();
        }
        currentPlan.getAllocationPlan().adjustForDuration(newDuration);
        previousDuration = recordDuration;

        // Display the form with the adjusted plan
        displayFormWithPlan(currentPlan.getAllocationPlan());
    }

    private void loadCurrentPlan(Integer factoryId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        allocationPersistenceService.getResourceAllocationPlanByFactoryId(factoryId)
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

            currentPlanStartDate.setText(currentPlan.getActivationDate().toLocalDate().toString());
            LocalDateTime planEndDate = currentPlan.getActivationDate().plusDays((long) Math.floor(currentPlan.getAllocationPlan().getDurationDays()));
            currentPlanEndDate.setText(planEndDate.toLocalDate().toString());

            displayFormWithPlan(currentPlan.getAllocationPlan());
        });
        return result;
    }

    private Result<ResourceAllocationPlan> handleCurrentPlanException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load current allocation plan."));
        return new Result<>();
    }

    private void displayFormWithPlan(AllocationPlan allocationPlan) {
        allocationsVBox.getChildren().clear();
        allocationsVBox.setSpacing(8);
        stageInputsGridPanes.clear();
        stageOutputsGridPanes.clear();

        groupRecordsByStageId(allocationPlan);

        int index = 0;

        for (Map.Entry<Integer, DailyProductionRecord> entry : recordsByStageId.entrySet()) {
            renderStageLabel(entry.getValue().getAllocations().getFirst().getStageName());

            renderStageInputs(entry.getValue().getAllocations());

            renderStageOutputs(entry.getValue().getResults());

            // Keep track of the  stage IDs for gathering the record DTO
            layoutIndexToStageId.put(index, entry.getKey());
            index++;
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

    private void renderStageLabel(String stageName) {
        Label stageNameLabel = new Label("Stage: " + stageName);
        stageNameLabel.getStyleClass().setAll("general-label-large");
        stageNameLabel.setStyle("-fx-padding: 8px 0 0 0;");
        allocationsVBox.getChildren().add(stageNameLabel);
    }

    private void renderStageInputs(List<ResourceAllocation> allocations) {
        Label stageInputLabel = new Label("• Stage Inputs");
        stageInputLabel.setStyle("-fx-padding: 8px 0 0 0; -fx-font-size: 16px; -fx-font-weight: bold;");
        allocationsVBox.getChildren().add(stageInputLabel);

        if (allocations == null) {
            stageInputLabel.setText("Stage Inputs: None");
            return;
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
        for (int i = 0; i < allocations.size(); i++) {
            renderStageInputRow(stageInputGridPane, allocations.get(i), i + 1);
        }

        stageInputsGridPanes.add(stageInputGridPane);
        allocationsVBox.getChildren().add(stageInputGridPane);
    }

    private void renderStageOutputs(List<AllocationResult> results) {
        Label stageOutputLabel = new Label("• Stage Outputs");
        stageOutputLabel.setStyle("-fx-padding: 8px 0 0 0; -fx-font-size: 16px; -fx-font-weight: bold;");
        allocationsVBox.getChildren().add(stageOutputLabel);

        if (results == null) {
            stageOutputLabel.setText("Stage Outputs: None");
            return;
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
        for (int i = 0; i < results.size(); i++) {
            renderStageOutputRow(stageOutputGridPane, results.get(i), i + 1);
        }

        stageOutputsGridPanes.add(stageOutputGridPane);
        allocationsVBox.getChildren().add(stageOutputGridPane);
    }

    private void renderColumnHeader(GridPane stageGridPane, String headerText, int columnIndex) {
        Label headerLabel = new Label(headerText);
        headerLabel.getStyleClass().setAll("general-label");

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(80);
        stageGridPane.getColumnConstraints().add(columnIndex, columnConstraints);

        stageGridPane.add(headerLabel, columnIndex, 0);
    }

    private void renderStageInputRow(GridPane stageGridPane, ResourceAllocation allocation, int rowIndex) {
        renderLabel(stageGridPane, allocation.getComponentName(), rowIndex, 0);

        TextField actualAmountField = new TextField();
        actualAmountField.getStyleClass().setAll("custom-text-field");
        stageGridPane.add(actualAmountField, 1, rowIndex);

        renderLabel(stageGridPane, String.valueOf(allocation.getAllocatedAmount()), rowIndex, 2);
        renderLabel(stageGridPane, String.valueOf(allocation.getRequestedAmount()), rowIndex, 3);
    }

    private void renderStageOutputRow(GridPane stageGridPane, AllocationResult allocationResult, int rowIndex) {
        renderLabel(stageGridPane, allocationResult.getComponentName(), rowIndex, 0);

        TextField actualAmountField = new TextField();
        actualAmountField.getStyleClass().setAll("custom-text-field");
        stageGridPane.add(actualAmountField, 1, rowIndex);

        renderLabel(stageGridPane, String.valueOf(allocationResult.getResultedAmount()), rowIndex, 2);
        renderLabel(stageGridPane, String.valueOf(allocationResult.getFullAmount()), rowIndex, 3);
    }

    private void renderLabel(GridPane stageGridPane, String text, int rowIndex, int columnIndex) {
        Label label = new Label(text);
        label.getStyleClass().setAll("general-label");
        stageGridPane.add(label, columnIndex, rowIndex);
    }

    // Submitting
    private void loadHistoryId(Integer factoryId) {
        historyService.getFactoryProductionHistoryByFactoryId(factoryId)
                .thenApply(result -> {
                    if (result.getError() != null) {
                        return result;
                    }
                    productionHistory = result.getData();
                    return result;
                });
    }

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        if (productionHistory == null) return;

        AddDayToFactoryProductionHistoryDTO recordDTO = gatherRecordDTO();

        historyService.addDayToFactoryProductionHistory(recordDTO)
                .thenApply(this::handleRecordAddition)
                .exceptionally(this::handleRecordAdditionException);
    }

    private Result<FactoryProductionHistory> handleRecordAddition(Result<FactoryProductionHistory> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to add production record.");
                return;
            }
            fallbackManager.setLoading(false);

            if (actionListener != null) {
                actionListener.onAddProductionRecord(result.getData());
            }
        });
        return result;
    }

    private Result<FactoryProductionHistory> handleRecordAdditionException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to add production record."));
        return new Result<>();
    }

    private AddDayToFactoryProductionHistoryDTO gatherRecordDTO() {
        AddDayToFactoryProductionHistoryDTO recordDTO = new AddDayToFactoryProductionHistoryDTO();
        recordDTO.setId(productionHistory.getId());
        recordDTO.setFactoryId(factory.getId());

        float daysSinceStart = (float) startDatePicker.getValue().toEpochDay() - productionHistory.getProductionHistory().getStartDate().toLocalDate().toEpochDay();
        recordDTO.setDaysSinceStart(daysSinceStart);

        DailyProductionRecord newRecord = new DailyProductionRecord();
        newRecord.setDurationDays(previousDuration);

        List<ResourceAllocation> recordAllocations = currentPlan.getAllocationPlan().getAllocations();
        List<AllocationResult> recordResults = currentPlan.getAllocationPlan().getResults();

        for (int index = 0; index < stageInputsGridPanes.size(); index++) {
            gatherStageInputAmounts(index);
        }

        for (int index = 0; index < stageOutputsGridPanes.size(); index++) {
            gatherStageOutputAmounts(index);
        }

        newRecord.setAllocations(recordAllocations);
        newRecord.setResults(recordResults);
        System.out.println("Record: " + newRecord);

        recordDTO.setDailyProductionRecord(newRecord);
        return recordDTO;
    }

    private void gatherStageInputAmounts(int index) {
        GridPane stageInputPane = stageInputsGridPanes.get(index);
        Integer factoryStageId = layoutIndexToStageId.get(index);
        List<ResourceAllocation> stageAllocations = recordsByStageId.get(factoryStageId).getAllocations();

        for (int i = 0; i < stageAllocations.size(); i++) {
            Node actualAmountNode = getNodeByRowColumnIndex(stageInputPane, i + 1, 1);

            if (actualAmountNode instanceof TextField actualAmountField) {
                Integer stageInputId = stageAllocations.get(i).getStageInputId();
                ResourceAllocation allocation = findAllocationByStageAndStageInputId(factoryStageId, stageInputId);
                if (allocation == null) continue;

                allocation.setActualAmount(Float.parseFloat(actualAmountField.getText()));
            }
        }
    }

    private void gatherStageOutputAmounts(int index) {
        GridPane stageOutputPane = stageOutputsGridPanes.get(index);
        Integer factoryStageId = layoutIndexToStageId.get(index);
        List<AllocationResult> stageResults = recordsByStageId.get(factoryStageId).getResults();

        for (int i = 0; i < stageResults.size(); i++) {
            Node actualAmountNode = getNodeByRowColumnIndex(stageOutputPane, i + 1, 1);

            if (actualAmountNode instanceof TextField actualAmountField) {
                Integer stageOutputId = stageResults.get(i).getStageOutputId();
                AllocationResult result = findResultByStageAndStageOutputId(factoryStageId, stageOutputId);
                if (result == null) continue;

                result.setActualAmount(Float.parseFloat(actualAmountField.getText()));
            }
        }
    }

    private ResourceAllocation findAllocationByStageAndStageInputId(Integer stageId, Integer stageInputId) {
        return currentPlan.getAllocationPlan().getAllocations().stream()
                .filter(allocation -> allocation.getFactoryStageId().equals(stageId) && allocation.getStageInputId().equals(stageInputId))
                .findFirst()
                .orElse(null);
    }

    private AllocationResult findResultByStageAndStageOutputId(Integer stageId, Integer stageOutputId) {
        return currentPlan.getAllocationPlan().getResults().stream()
                .filter(result -> result.getFactoryStageId().equals(stageId) && result.getStageOutputId().equals(stageOutputId))
                .findFirst()
                .orElse(null);
    }

    private Node getNodeByRowColumnIndex(GridPane gridPane, final int row, final int column) {
        Node result = null;
        for (Node node : gridPane.getChildren()) {
            if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }
}
