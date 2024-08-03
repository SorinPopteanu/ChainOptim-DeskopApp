package org.chainoptim.desktop.features.supplier.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.supplier.dto.SupplierOverviewDTO;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.service.SupplierService;
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

public class SupplierOverviewController implements DataReceiver<Supplier> {

    private final SupplierService supplierService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;

    private final FallbackManager fallbackManager;

    private Supplier supplier;

    @FXML
    private VBox detailsVBox;
    @FXML
    private VBox entitiesVBox;

    @Inject
    public SupplierOverviewController(SupplierService supplierService,
                                      NavigationService navigationService,
                                      CurrentSelectionService currentSelectionService,
                                      FallbackManager fallbackManager) {
        this.supplierService = supplierService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Supplier supplier) {
        this.supplier = supplier;

        loadSupplierOverview();
    }

    private void loadSupplierOverview() {
        if (supplier == null) {
            return;
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        supplierService.getSupplierOverview(supplier.getId())
                .thenApply(this::handleOverviewResponse)
                .exceptionally(this::handleOverviewException);
    }

    private Result<SupplierOverviewDTO> handleOverviewResponse(Result<SupplierOverviewDTO> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load supplier overview");
                return;
            }
            fallbackManager.setLoading(false);

            SupplierOverviewDTO supplierOverviewDTO = result.getData();

            renderDates();
            renderLocation();
            renderEntityFlowPane("Supplied Components", supplierOverviewDTO.getSuppliedComponents(), "Component");
        });
        return result;
    }

    private Result<SupplierOverviewDTO> handleOverviewException(Throwable throwable) {
        Platform.runLater(() ->
            fallbackManager.setErrorMessage("Failed to load supplier overview")
        );
        return new Result<>();
    }

    private void renderDates() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy, hh:mm a");
        String formattedCreatedAt = supplier.getCreatedAt() != null ? supplier.getCreatedAt().format(dateTimeFormatter) : "None";
        String formattedUpdatedAt = supplier.getUpdatedAt() != null ? supplier.getUpdatedAt().format(dateTimeFormatter) : "None";
        renderField("Created At: ", formattedCreatedAt);
        renderField("Last Modified: ", formattedUpdatedAt);
    }

    private void renderLocation() {
        if (supplier.getLocation() == null) {
            return;
        }

        Label locationLabel = new Label("Location");
        locationLabel.getStyleClass().setAll("general-label-large");
        detailsVBox.getChildren().add(locationLabel);

        renderField("Address: ", supplier.getLocation().getAddress());
        renderField("City: ", supplier.getLocation().getCity());
        renderField("State: ", supplier.getLocation().getState());
        renderField("Country: ", supplier.getLocation().getCountry());
        renderField("Zip Code: ", supplier.getLocation().getZipCode());
        renderField("Latitude: ", supplier.getLocation().getLatitude() != null ? supplier.getLocation().getLatitude().toString() : "");
        renderField("Longitude: ", supplier.getLocation().getLongitude() != null ? supplier.getLocation().getLongitude().toString() : "");
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
