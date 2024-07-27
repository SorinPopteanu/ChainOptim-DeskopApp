package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.features.warehouse.model.*;
import org.chainoptim.desktop.features.warehouse.service.CompartmentService;
import org.chainoptim.desktop.features.warehouse.service.CrateService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class WarehouseStorageController implements DataReceiver<Warehouse> {

    // Services
    private final CompartmentService compartmentService;
    private final CrateService crateService;

    // State
    private final FallbackManager fallbackManager;

    private Warehouse warehouse;
    private List<Crate> crates;

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
        this.warehouse = warehouse;

        if (TenantContext.getCurrentUser() == null) {
            fallbackManager.setErrorMessage("User not logged in");
            return;
        }
        Integer organizationId = TenantContext.getCurrentUser().getOrganization().getId();
        loadCrates(organizationId);
    }

    private void loadCrates(Integer organizationId) {
        crateService.getCratesByOrganizationId(organizationId)
                .thenApply(this::handleCratesResponse)
                .exceptionally(this::handleCratesError);
    }

    private Result<List<Crate>> handleCratesResponse(Result<List<Crate>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage(result.getError().getMessage());
                return;
            }

            fallbackManager.setLoading(false);
            crates = result.getData();

            loadCompartments(this.warehouse.getId());
        });

        return result;
    }

    private Result<List<Crate>> handleCratesError(Throwable throwable) {
        fallbackManager.setErrorMessage("Failed to load crates");

        return new Result<>();
    }


    private void loadCompartments(Integer warehouseId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        compartmentService.getCompartmentsByWarehouseId(warehouseId)
                .thenApply(this::handleCompartmentsResponse)
                .exceptionally(this::handleCompartmentsError);
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
        compartmentsVBox.setSpacing(10);

        for (Compartment compartment : compartments) {
            FlowPane compartmentFlowPlane = new FlowPane(16, 8);
            compartmentFlowPlane.setAlignment(Pos.CENTER_LEFT);
            compartmentsVBox.getChildren().add(compartmentFlowPlane);

            Label compartmentName = new Label(compartment.getName());
            compartmentName.getStyleClass().add("entity-name-label");
            compartmentFlowPlane.getChildren().add(compartmentName);

            List<CrateSpec> crateSpecs = compartment.getData().getCrateSpecs();
            if (crateSpecs == null || crateSpecs.isEmpty()) {
                continue;
            }

            for (CrateSpec crateSpec : crateSpecs) {
                Integer crateId = crateSpec.getCrateId();
                if (crateId == null) {
                    continue;
                }

                CrateData crateData = compartment.getData().getCurrentCrates().stream()
                        .filter(cd -> cd.getCrateId().equals(crateId))
                        .findFirst()
                        .orElse(null);

                float numberOfCrates = 0;
                float maxCrates = 1;
                if (crateData == null) {
                    continue;
                }
                numberOfCrates = crateData.getNumberOfCrates();
                maxCrates = crateSpec.getMaxCrates();
                double occupiedRatio = (double) numberOfCrates / maxCrates;

                Crate correspCrate = crates.stream()
                        .filter(c -> c.getId().equals(crateId))
                        .findFirst()
                        .orElse(null);
                if (correspCrate == null) {
                    continue;
                }

                HBox crateHBox = new HBox(8);
                crateHBox.setStyle("-fx-padding: 8px; -fx-border-color: #E0E0E0; -fx-border-width: 1px; -fx-border-radius: 4px;");
                compartmentFlowPlane.getChildren().add(crateHBox);

                Label crateName = new Label(correspCrate.getName());
                crateName.getStyleClass().add("general-label");
                crateHBox.getChildren().add(crateName);

                ProgressBar progressBar = new ProgressBar();
                progressBar.setProgress(occupiedRatio);
                crateHBox.getChildren().add(progressBar);
                updateProgressBarColor(progressBar, occupiedRatio);

                Label occupiedRatioLabel = new Label(String.format("%.2f", occupiedRatio * 100) + "%");
                occupiedRatioLabel.getStyleClass().add("general-label");
                crateHBox.getChildren().add(occupiedRatioLabel);
            }
        }
    }

    private void updateProgressBarColor(ProgressBar progressBar, double progress) {
        if (progress < 0.25) {
            progressBar.setStyle("-fx-accent: #4CAF50;");
        } else if (progress < 0.5) {
            progressBar.setStyle("-fx-accent: #FF7934;");
        } else if (progress < 0.75) {
            progressBar.setStyle("-fx-accent: #FFC107;");
        } else {
            progressBar.setStyle("-fx-accent: #F44336;");
        }
    }

    @FXML
    private void handleAddCompartment() {
        System.out.println("Add compartment");
    }
}
