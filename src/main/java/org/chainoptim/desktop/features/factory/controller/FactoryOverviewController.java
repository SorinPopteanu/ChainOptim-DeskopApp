package org.chainoptim.desktop.features.factory.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.factory.dto.FactoryOverviewDTO;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.service.FactoryService;
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

public class FactoryOverviewController implements DataReceiver<Factory> {

    private final FactoryService factoryService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;

    private final FallbackManager fallbackManager;

    private Factory factory;

    @FXML
    private VBox detailsVBox;
    @FXML
    private VBox entitiesVBox;

    @Inject
    public FactoryOverviewController(FactoryService factoryService,
                                     NavigationService navigationService,
                                     CurrentSelectionService currentSelectionService,
                                     FallbackManager fallbackManager) {
        this.factoryService = factoryService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Factory factory) {
        this.factory = factory;

        loadFactoryOverview();
    }

    private void loadFactoryOverview() {
        if (factory == null) {
            return;
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryService.getFactoryOverview(factory.getId())
                .thenApply(this::handleOverviewResponse)
                .exceptionally(this::handleOverviewException);
    }

    private Result<FactoryOverviewDTO> handleOverviewResponse(Result<FactoryOverviewDTO> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load factory overview");
                return;
            }
            fallbackManager.setLoading(false);
            FactoryOverviewDTO factoryOverviewDTO = result.getData();

            renderUI(factoryOverviewDTO);
        });
        return result;
    }

    private Result<FactoryOverviewDTO> handleOverviewException(Throwable throwable) {
        Platform.runLater(() ->
            fallbackManager.setErrorMessage("Failed to load factory overview")
        );
        return new Result<>();
    }

    private void renderUI(FactoryOverviewDTO factoryOverviewDTO) {
        renderDates();
        renderLocation();
        renderEntityFlowPane("Factory Stages", factoryOverviewDTO.getFactoryStages(), "Stage");
        renderEntityFlowPane("Manufactured Components", factoryOverviewDTO.getManufacturedComponents(), "Component");
        renderEntityFlowPane("Manufactured Products", factoryOverviewDTO.getManufacturedProducts(), "Product");
        renderEntityFlowPane("Delivered From Suppliers", factoryOverviewDTO.getDeliveredFromSuppliers(), "Supplier");
        renderEntityFlowPane("Delivered To Clients", factoryOverviewDTO.getDeliveredToClients(), "Client");
    }

    private void renderDates() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy, hh:mm a");
        String formattedCreatedAt = factory.getCreatedAt() != null ? factory.getCreatedAt().format(dateTimeFormatter) : "None";
        String formattedUpdatedAt = factory.getUpdatedAt() != null ? factory.getUpdatedAt().format(dateTimeFormatter) : "None";
        renderField("Created At: ", formattedCreatedAt);
        renderField("Last Modified: ", formattedUpdatedAt);
    }

    private void renderLocation() {
        if (factory.getLocation() == null) {
            return;
        }

        Label locationLabel = new Label("• Location");
        locationLabel.getStyleClass().setAll("general-label-large");
        detailsVBox.getChildren().add(locationLabel);

        renderField("Address: ", factory.getLocation().getAddress());
        renderField("City: ", factory.getLocation().getCity());
        renderField("State: ", factory.getLocation().getState());
        renderField("Country: ", factory.getLocation().getCountry());
        renderField("Zip Code: ", factory.getLocation().getZipCode());
        renderField("Latitude: ", factory.getLocation().getLatitude() != null ? factory.getLocation().getLatitude().toString() : "");
        renderField("Longitude: ", factory.getLocation().getLongitude() != null ? factory.getLocation().getLongitude().toString() : "");
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
