package org.chainoptim.desktop.features.warehouse.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.warehouse.dto.WarehouseOverviewDTO;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.warehouse.service.WarehouseService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.dto.SmallEntityDTO;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class WarehouseOverviewController implements DataReceiver<Warehouse> {

    private final WarehouseService warehouseService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;

    private final FallbackManager fallbackManager;

    private Warehouse warehouse;

    @FXML
    private VBox detailsVBox;
    @FXML
    private VBox entitiesVBox;

    @Inject
    public WarehouseOverviewController(WarehouseService warehouseService,
                                       NavigationService navigationService,
                                       CurrentSelectionService currentSelectionService,
                                       FallbackManager fallbackManager) {
        this.warehouseService = warehouseService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Warehouse warehouse) {
        this.warehouse = warehouse;

        loadWarehouseOverview();
    }

    private void loadWarehouseOverview() {
        if (warehouse == null) {
            return;
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        warehouseService.getWarehouseOverview(warehouse.getId())
                .thenApply(this::handleOverviewResponse)
                .exceptionally(this::handleOverviewException);
    }

    private Result<WarehouseOverviewDTO> handleOverviewResponse(Result<WarehouseOverviewDTO> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load warehouse overview");
                return;
            }
            fallbackManager.setLoading(false);
            WarehouseOverviewDTO warehouseOverviewDTO = result.getData();

            renderUI(warehouseOverviewDTO);
        });
        return result;
    }

    private Result<WarehouseOverviewDTO> handleOverviewException(Throwable throwable) {
        Platform.runLater(() ->
            fallbackManager.setErrorMessage("Failed to load warehouse overview")
        );
        return new Result<>();
    }

    private void renderUI(WarehouseOverviewDTO warehouseOverviewDTO) {
        renderDates();
        renderLocation();
        renderEntityFlowPane("Compartments", warehouseOverviewDTO.getCompartments(), "Compartment");
        renderEntityFlowPane("Stored Components", warehouseOverviewDTO.getStoredComponents(), "Component");
        renderEntityFlowPane("Stored Products", warehouseOverviewDTO.getStoredProducts(), "Product");
        renderEntityFlowPane("Delivered From Suppliers", warehouseOverviewDTO.getDeliveredFromSuppliers(), "Supplier");
        renderEntityFlowPane("Delivered To Clients", warehouseOverviewDTO.getDeliveredToClients(), "Client");
    }

    private void renderDates() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy, hh:mm a");
        String formattedCreatedAt = warehouse.getCreatedAt() != null ? warehouse.getCreatedAt().format(dateTimeFormatter) : "None";
        String formattedUpdatedAt = warehouse.getUpdatedAt() != null ? warehouse.getUpdatedAt().format(dateTimeFormatter) : "None";
        renderField("Created At: ", formattedCreatedAt);
        renderField("Last Modified: ", formattedUpdatedAt);
    }

    private void renderLocation() {
        if (warehouse.getLocation() == null) {
            return;
        }

        Label locationLabel = new Label("• Location");
        locationLabel.getStyleClass().setAll("general-label-large");
        detailsVBox.getChildren().add(locationLabel);

        renderField("Address: ", warehouse.getLocation().getAddress());
        renderField("City: ", warehouse.getLocation().getCity());
        renderField("State: ", warehouse.getLocation().getState());
        renderField("Country: ", warehouse.getLocation().getCountry());
        renderField("Zip Code: ", warehouse.getLocation().getZipCode());
        renderField("Latitude: ", warehouse.getLocation().getLatitude() != null ? warehouse.getLocation().getLatitude().toString() : "");
        renderField("Longitude: ", warehouse.getLocation().getLongitude() != null ? warehouse.getLocation().getLongitude().toString() : "");
    }

    private void renderField(String field, String fieldValue) {
        HBox fieldHBox = new HBox(8);
        fieldHBox.setAlignment(Pos.CENTER_LEFT);
        Label countryLabel = new Label(field);
        countryLabel.getStyleClass().setAll("general-label-medium-large");

        Label countryValueLabel = new Label();
        countryValueLabel.getStyleClass().setAll("general-label");
        countryValueLabel.setText(fieldValue);

        if (fieldValue != null) {
            fieldHBox.getChildren().addAll(countryLabel, countryValueLabel);
            detailsVBox.getChildren().add(fieldHBox);
        }
    }

    private void renderEntityFlowPane(String labelText, List<SmallEntityDTO> entityDTOs, String entityPageKey) {
        FlowPane entityFlowPane = new FlowPane();
        entityFlowPane.setHgap(8);
        entityFlowPane.setVgap(8);
        entityFlowPane.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText + ":");
        label.getStyleClass().setAll("general-label-medium-large");
        entityFlowPane.getChildren().add(label);

        if (entityDTOs.isEmpty()) {
            Label noEntitiesLabel = new Label("None");
            noEntitiesLabel.getStyleClass().setAll("general-label");
            entityFlowPane.getChildren().add(noEntitiesLabel);
            entitiesVBox.getChildren().add(entityFlowPane);
            return;
        }

        for (int i = 0; i < entityDTOs.size(); i++) {
            SmallEntityDTO entityDTO = entityDTOs.get(i);
            String nameText = i != entityDTOs.size() - 1 ? entityDTO.getName() + ", " : entityDTO.getName();
            Label entityLabel = new Label(nameText);
            entityLabel.getStyleClass().setAll("pseudo-link", "general-label");

            entityLabel.setOnMouseClicked(event -> {
                currentSelectionService.setSelectedId(entityDTO.getId());
                navigationService.switchView(entityPageKey + "?id=" + entityDTO.getId(), true, null);
            });

            entityFlowPane.getChildren().add(entityLabel);
        }

        entitiesVBox.getChildren().add(entityFlowPane);
    }


}
