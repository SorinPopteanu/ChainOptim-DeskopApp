package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.features.warehouse.model.Compartment;
import org.chainoptim.desktop.features.warehouse.model.Crate;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.warehouse.service.CompartmentService;
import org.chainoptim.desktop.features.warehouse.service.CrateService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class WarehouseStorageController implements DataReceiver<Warehouse> {

    // Services
    private final CompartmentService compartmentService;
    private final CrateService crateService;

    // State
    private final FallbackManager fallbackManager;

    // FXML
    @FXML
    private VBox compartmentsVBox;

    @Inject
    public WarehouseStorageController(CompartmentService compartmentService,
                                      CrateService crateService,
                                      FallbackManager fallbackManager) {
        this.compartmentService = compartmentService;
        this.crateService = crateService;
        this.fallbackManager = fallbackManager;
    }

    public void setData(Warehouse warehouse) {
        loadCompartments(warehouse.getId());

        if (TenantContext.getCurrentUser() == null) {
            fallbackManager.setErrorMessage("User not logged in");
            return;
        }
        Integer organizationId = TenantContext.getCurrentUser().getOrganization().getId();
        loadCrates(organizationId);
    }

    private void loadCompartments(Integer warehouseId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        compartmentService.getCompartmentsByWarehouseId(warehouseId)
            .thenApply(this::handleCompartmentsResponse)
            .exceptionally(this::handleCompartmentsError);
    }
    
    private void loadCrates(Integer organizationId) {
        crateService.getCratesByOrganizationId(organizationId)
            .thenApply(this::handleCratesResponse)
            .exceptionally(this::handleCratesError);
    }

    private Result<List<Compartment>> handleCompartmentsResponse(Result<List<Compartment>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage(result.getError().getMessage());
                return;
            }

            fallbackManager.setLoading(false);

            renderCompartments(result.getData());
        });

        return result;
    }

    private Result<List<Compartment>> handleCompartmentsError(Throwable throwable) {
        fallbackManager.setErrorMessage("Failed to load compartments");

        return new Result<>();
    }

    private void renderCompartments(List<Compartment> compartments) {
        compartmentsVBox.getChildren().clear();

        for (Compartment compartment : compartments) {
            Label compartmentName = new Label(compartment.getName());
            compartmentName.getStyleClass().add("entity-name-label");

            compartmentsVBox.getChildren().add(compartmentName);
        }

        fallbackManager.setNoResults(false);
    }

    private Result<List<Crate>> handleCratesResponse(Result<List<Crate>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage(result.getError().getMessage());
                return;
            }

            fallbackManager.setLoading(false);
            System.out.println("Crates loaded successfully");
//            renderCrates(result.getData());
        });

        return result;
    }

    private Result<List<Crate>> handleCratesError(Throwable throwable) {
        fallbackManager.setErrorMessage("Failed to load crates");

        return new Result<>();
    }
}
