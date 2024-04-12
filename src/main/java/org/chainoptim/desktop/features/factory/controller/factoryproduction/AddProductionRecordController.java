package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.DailyProductionRecord;
import org.chainoptim.desktop.features.scanalysis.productionhistory.service.FactoryProductionHistoryService;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocation;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationPersistenceService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

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
    private VBox resultsVBox;

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

        float recordDuration = endDatePicker.getValue().toEpochDay() - (float) startDatePicker.getValue().toEpochDay();
        if (recordDuration < 0) return;

        currentPlan.getAllocationPlan().adjustForDuration(recordDuration);
        displayFormWithCurrentPlan();
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
            fallbackManager.setLoading(false);

            displayFormWithCurrentPlan();
        });
        return planOptional;
    }

    private Optional<ResourceAllocationPlan> handleCurrentPlanException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load current allocation plan."));
        return Optional.empty();
    }

    private void displayFormWithCurrentPlan() {
        currentPlanStartDate.setText(currentPlan.getActivationDate().toLocalDate().toString());
        LocalDateTime planEndDate = currentPlan.getActivationDate().plusDays((long) Math.floor(currentPlan.getAllocationPlan().getDurationDays()));
        currentPlanEndDate.setText(planEndDate.toLocalDate().toString());

        newRecord = new DailyProductionRecord();
        newRecord.setAllocations(
                currentPlan.getAllocationPlan().getAllocations().stream()
                        .sorted(Comparator.comparingInt(ResourceAllocation::getFactoryStageId))
                        .toList()
        );

        String currentStage = newRecord.getAllocations().getFirst().getStageName();

        allocationsVBox.getChildren().clear();
        allocationsVBox.setSpacing(8);

        for (ResourceAllocation allocation : newRecord.getAllocations()) {
            VBox allocationVBox = new VBox(8);

            if (!Objects.equals(allocation.getStageName(), currentStage) || allocationsVBox.getChildren().isEmpty()) {
                Label stageNameLabel = addStageLabel(currentStage);
                allocationsVBox.getChildren().add(stageNameLabel);
            }
            currentStage = allocation.getStageName();

            HBox amountsHBox = renderInputAmounts(allocation);
            allocationVBox.getChildren().add(amountsHBox);

            allocationsVBox.getChildren().add(allocationVBox);
        }

    }

    private Label addStageLabel(String stageName) {
        Label stageNameLabel = new Label("Stage: " + stageName);
        stageNameLabel.getStyleClass().setAll("general-label-large");
        stageNameLabel.setStyle("-fx-padding: 8px 0 0 0;");
        return stageNameLabel;
    }

    private HBox renderInputAmounts(ResourceAllocation allocation) {
        HBox amountsHBox = new HBox(8);
        amountsHBox.setAlignment(Pos.CENTER_LEFT);

        Label componentNameLabel = new Label("Component: " + allocation.getComponentName());
        componentNameLabel.getStyleClass().setAll("general-label");
        componentNameLabel.setStyle("-fx-padding: 4px;");
        amountsHBox.getChildren().add(componentNameLabel);



        VBox actualAmountVBox = new VBox(8);
        Label actualAmountLabel = new Label("Allocated Amount");
        actualAmountLabel.getStyleClass().setAll("general-label");
        TextField actualAmountField = new TextField();
        actualAmountField.getStyleClass().setAll("custom-text-field");
        actualAmountVBox.getChildren().addAll(actualAmountLabel, actualAmountField);
        amountsHBox.getChildren().add(actualAmountVBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        amountsHBox.getChildren().add(spacer);

        VBox plannedAmountVBox = new VBox(8);
        Label plannedAmountLabel = new Label("Planned Amount");
        plannedAmountLabel.getStyleClass().setAll("general-label");
        Label plannedAmountValue = new Label(String.valueOf(allocation.getAllocatedAmount()));
        plannedAmountValue.getStyleClass().setAll("general-label");
        plannedAmountValue.setStyle("-fx-padding: 4px 0 0 0; -fx-text-fill: #121212");
        plannedAmountVBox.getChildren().addAll(plannedAmountLabel, plannedAmountValue);
        amountsHBox.getChildren().add(plannedAmountVBox);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        amountsHBox.getChildren().add(spacer2);

        VBox neededAmountVBox = new VBox(8);
        Label neededAmountLabel = new Label("Needed Amount");
        neededAmountLabel.getStyleClass().setAll("general-label");
        Label neededAmountValue = new Label(String.valueOf(allocation.getRequestedAmount()));
        neededAmountValue.getStyleClass().setAll("general-label");
        neededAmountValue.setStyle("-fx-padding: 4px 0 0 0; -fx-text-fill: #121212");
        neededAmountVBox.getChildren().addAll(neededAmountLabel, neededAmountValue);
        amountsHBox.getChildren().add(neededAmountVBox);

        return amountsHBox;
    }
}
//
//OK I have this:
//
//public class AddProductionRecordController implements DataReceiver<Factory> {
//
//    // Services
//    private final FactoryProductionHistoryService historyService;
//    private final ResourceAllocationPersistenceService allocationPersistenceService;
//    private final CommonViewsLoader commonViewsLoader;
//
//    // State
//    private final FallbackManager fallbackManager;
//    private ResourceAllocationPlan currentPlan;
//    private Factory factory;
//    private DailyProductionRecord newRecord;
//
//    @FXML
//    private Label currentPlanStartDate;
//    @FXML
//    private Label currentPlanEndDate;
//    @FXML
//    private DatePicker startDatePicker;
//    @FXML
//    private DatePicker endDatePicker;
//    @FXML
//    private VBox allocationsVBox;
//
//    @Inject
//    public AddProductionRecordController(FactoryProductionHistoryService historyService,
//                                         ResourceAllocationPersistenceService allocationPersistenceService,
//                                         CommonViewsLoader commonViewsLoader,
//                                         FallbackManager fallbackManager) {
//        this.historyService = historyService;
//        this.allocationPersistenceService = allocationPersistenceService;
//        this.commonViewsLoader = commonViewsLoader;
//        this.fallbackManager = fallbackManager;
//    }
//
//    @Override
//    public void setData(Factory factory) {
//        this.factory = factory;
//        setUpListeners();
//        loadCurrentPlan(factory.getId());
//    }
//
//    private void setUpListeners() {
//        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> rerenderPlan());
//
//        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> rerenderPlan());
//    }
//
//    private void rerenderPlan() {
//        if (currentPlan == null) return;
//        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) return;
//
//        float recordDuration = endDatePicker.getValue().toEpochDay() - (float) startDatePicker.getValue().toEpochDay();
//        if (recordDuration < 0) return;
//
//        currentPlan.getAllocationPlan().adjustForDuration(recordDuration);
//        displayFormWithCurrentPlan();
//    }
//
//    where:
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public class AllocationPlan {
//
//        private FactoryGraph factoryGraph;
//
//        private Map<Integer, FactoryInventoryItem> inventoryBalance;
//
//        private List<ResourceAllocation> allocations;
//        private List<AllocationResult> results;
//        private Float durationDays;
//
//        public void adjustForDuration(Float duration) {
//            float durationRatio = durationDays != 0 ? duration / durationDays : 1;
//
//            for (ResourceAllocation allocation : allocations) {
//                allocation.setRequestedAmount(allocation.getRequestedAmount() * durationRatio);
//                allocation.setAllocatedAmount(allocation.getAllocatedAmount() * durationRatio);
//            }
//
//            for (AllocationResult result : results) {
//                result.setFullAmount(result.getFullAmount() * durationRatio);
//                result.setResultedAmount(result.getResultedAmount() * durationRatio);
//            }
//        }
//    }
//    The current problem is that if I pick dates twice the second time it adjusts the plan obtained from the first of course, instead of the original plan. How can we fix this? Cause I know java and deep copies dont fare well