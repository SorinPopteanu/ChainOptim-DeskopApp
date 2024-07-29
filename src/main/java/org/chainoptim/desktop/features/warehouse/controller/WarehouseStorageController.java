package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.features.warehouse.dto.CreateCompartmentDTO;
import org.chainoptim.desktop.features.warehouse.model.*;
import org.chainoptim.desktop.features.warehouse.service.CompartmentService;
import org.chainoptim.desktop.features.warehouse.service.CrateService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class WarehouseStorageController implements DataReceiver<Warehouse> {

    // Services
    private final CompartmentService compartmentService;
    private final CrateService crateService;

    // State
    private final FallbackManager fallbackManager;

    private Warehouse warehouse;
    private List<Crate> crates;
    private final BooleanProperty isCreatingCompartment = new SimpleBooleanProperty(false);
    private CompartmentData newCompartmentData = new CompartmentData();

    // FXML
    @FXML
    private VBox compartmentsVBox;
    @FXML
    private Button addCompartmentButton;

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

        setUpListeners();

        if (TenantContext.getCurrentUser() == null) {
            fallbackManager.setErrorMessage("User not logged in");
            return;
        }
        Integer organizationId = TenantContext.getCurrentUser().getOrganization().getId();
        loadCrates(organizationId);
    }

    private void setUpListeners() {
        isCreatingCompartment.addListener((observable, oldValue, newValue) -> {
            addCompartmentButton.setDisable(newValue);
            addCompartmentButton.getStyleClass().clear();
            addCompartmentButton.getStyleClass().add(Boolean.TRUE.equals(newValue) ? "standard-write-button-disabled" : "standard-write-button");
        });
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
            System.out.println("Result: " + result);
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
            renderCompartment(compartment);
        }
    }

    private void renderCompartment(Compartment compartment) {
        FlowPane compartmentFlowPlane = new FlowPane(16, 16);
        compartmentFlowPlane.setMinWidth(800);
        compartmentFlowPlane.setAlignment(Pos.CENTER_LEFT);
        compartmentsVBox.getChildren().add(compartmentFlowPlane);

        Label compartmentName = new Label(compartment.getName());
        compartmentName.getStyleClass().add("entity-name-label");
        compartmentFlowPlane.getChildren().add(compartmentName);

        CompartmentData compartmentData = compartment.getData();
        if (compartmentData == null) {
            return;
        }
        List<CrateSpec> crateSpecs = compartmentData.getCrateSpecs();
        if (crateSpecs == null || crateSpecs.isEmpty()) {
            return;
        }

        for (CrateSpec crateSpec : crateSpecs) {
            Integer crateId = crateSpec.getCrateId();
            if (crateId == null) {
                continue;
            }

            float occupiedRatio = determineOccupiedRation(compartment, crateSpec, crateId);

            Crate correspCrate = crates.stream()
                    .filter(c -> c.getId().equals(crateId))
                    .findFirst()
                    .orElse(null);
            if (correspCrate == null) {
                continue;
            }

            renderCrates(compartmentFlowPlane, correspCrate, occupiedRatio);
        }
    }

    private float determineOccupiedRation(Compartment compartment, CrateSpec crateSpec, Integer crateId) {
        float numberOfCrates = 0;
        float maxCrates = 1;

        if (compartment.getData().getCurrentCrates() == null) {
            return 0.0f;
        }
        CrateData crateData = compartment.getData().getCurrentCrates().stream()
                .filter(cd -> cd.getCrateId().equals(crateId))
                .findFirst()
                .orElse(null);
        if (crateData == null) {
            return 0.0f;
        }

        numberOfCrates = crateData.getNumberOfCrates();

        maxCrates = crateSpec.getMaxCrates();
        return numberOfCrates / maxCrates;
    }

    private void renderCrates(FlowPane compartmentFlowPane, Crate correspCrate, double occupiedRatio) {
        HBox crateHBox = new HBox(8);
        crateHBox.setStyle("-fx-padding: 8px; -fx-border-color: #E0E0E0; -fx-border-width: 1px; -fx-border-radius: 4px;");
        compartmentFlowPane.getChildren().add(crateHBox);

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
        isCreatingCompartment.set(true);

        VBox compartmentCreationVBox = new VBox(16);
        compartmentsVBox.getChildren().add(compartmentCreationVBox);

        VBox cratesVBox = new VBox(16);

        HBox newCompartmentHBox = new HBox(16);
        newCompartmentHBox.setAlignment(Pos.CENTER_LEFT);
        compartmentCreationVBox.getChildren().add(newCompartmentHBox);

        TextField compartmentNameField = new TextField();
        compartmentNameField.getStyleClass().add("custom-text-field");
        compartmentNameField.setPromptText("Compartment name");
        newCompartmentHBox.getChildren().add(compartmentNameField);

        Button newCrateButton = new Button("Add Allowed Crates");
        newCrateButton.getStyleClass().add("standard-action-button");
        newCrateButton.setOnAction(event -> handleAddCrate(cratesVBox));
        newCompartmentHBox.getChildren().add(newCrateButton);

        Button confirmButton = new Button("Confirm Creation");
        confirmButton.getStyleClass().add("standard-write-button");
        confirmButton.setOnAction(event -> handleCreateCompartment(compartmentNameField, cratesVBox));
        newCompartmentHBox.getChildren().add(confirmButton);

        compartmentCreationVBox.getChildren().add(cratesVBox);
    }

    private void handleAddCrate(VBox cratesVBox) {
        HBox crateHBox = new HBox(16);
        cratesVBox.getChildren().add(crateHBox);

        ComboBox<Crate> crateComboBox = new ComboBox<>();
        crateComboBox.getStyleClass().add("custom-combo-box");
        crateComboBox.getItems().addAll(crates);
        crateComboBox.setPromptText("Select crate");
        crateHBox.getChildren().add(crateComboBox);

        crateComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Crate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        crateComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Crate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        TextField maxCratesField = new TextField();
        maxCratesField.getStyleClass().add("custom-text-field");
        maxCratesField.setPromptText("Max crates allowed");
        crateHBox.getChildren().add(maxCratesField);
    }

    private void handleCreateCompartment(TextField compartmentNameField, VBox cratesVBox) {
        CreateCompartmentDTO compartmentDTO = getCompartmentDTO(compartmentNameField.getText(), cratesVBox);

        compartmentService.createCompartment(compartmentDTO)
                .thenApply(this::handleCreateCompartmentResponse)
                .exceptionally(this::handleCreateCompartmentError);
    }

    private CreateCompartmentDTO getCompartmentDTO(String compartmentName, VBox cratesVBox) {
        CreateCompartmentDTO compartmentDTO = new CreateCompartmentDTO();
        compartmentDTO.setWarehouseId(warehouse.getId());
        compartmentDTO.setName(compartmentName);
        compartmentDTO.setOrganizationId(TenantContext.getCurrentUser().getOrganization().getId());

        CompartmentData compartmentData = new CompartmentData();
        compartmentDTO.setData(compartmentData);
        compartmentData.setCrateSpecs(new ArrayList<>());

        for (Node node : cratesVBox.getChildren()) {
            CrateSpec crateSpec = findCrateSpecs(node);
            if (crateSpec != null) {
                compartmentDTO.getData().getCrateSpecs().add(crateSpec);
            }
        }

        return compartmentDTO;
    }

    private CrateSpec findCrateSpecs(Node node) {
        if (!(node instanceof HBox crateHBox)) {
            return null;
        }

        ComboBox<Crate> crateComboBox = (ComboBox<Crate>) crateHBox.getChildren().getFirst();
        TextField maxCratesField = (TextField) crateHBox.getChildren().get(1);

        Crate crate = crateComboBox.getValue();
        if (crate == null) {
            return null;
        }

        Float maxCrates = Float.parseFloat(maxCratesField.getText());
        CrateSpec crateSpec = new CrateSpec();
        crateSpec.setCrateId(crate.getId());
        crateSpec.setMaxCrates(maxCrates);

        return crateSpec;
    }

    private Result<Compartment> handleCreateCompartmentResponse(Result<Compartment> compartment) {
        Platform.runLater(() -> {
            if (compartment.getError() != null) {
                fallbackManager.setErrorMessage(compartment.getError().getMessage());
                return;
            }

            compartmentsVBox.getChildren().removeLast();

            renderCompartment(compartment.getData());

            isCreatingCompartment.set(false);
        });
        return compartment;
    }

    private Result<Compartment> handleCreateCompartmentError(Throwable throwable) {
        fallbackManager.setErrorMessage("Failed to create compartment");
        return new Result<>();
    }
}
