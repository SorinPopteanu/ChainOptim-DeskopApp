package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.scanalysis.productionhistory.service.FactoryProductionHistoryService;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationPersistenceService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
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

    @FXML
    private Label currentPlanStartDate;
    @FXML
    private Label currentPlanEndDate;
    @FXML
    private DatePicker startDatePicker;

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

        loadCurrentPlan(factory.getId());
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
    }
}
