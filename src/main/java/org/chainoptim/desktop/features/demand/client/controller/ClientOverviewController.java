package org.chainoptim.desktop.features.demand.client.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.demand.client.dto.ClientOverviewDTO;
import org.chainoptim.desktop.features.demand.client.model.Client;
import org.chainoptim.desktop.features.demand.client.service.ClientService;
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

public class ClientOverviewController implements DataReceiver<Client> {

    private final ClientService clientService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;

    private final FallbackManager fallbackManager;

    private Client client;

    @FXML
    private VBox detailsVBox;
    @FXML
    private VBox entitiesVBox;

    @Inject
    public ClientOverviewController(ClientService clientService,
                                    NavigationService navigationService,
                                    CurrentSelectionService currentSelectionService,
                                    FallbackManager fallbackManager) {
        this.clientService = clientService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Client client) {
        this.client = client;

        loadClientOverview();
    }

    private void loadClientOverview() {
        if (client == null) {
            return;
        }

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientService.getClientOverview(client.getId())
                .thenApply(this::handleOverviewResponse)
                .exceptionally(this::handleOverviewException);
    }

    private Result<ClientOverviewDTO> handleOverviewResponse(Result<ClientOverviewDTO> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load client overview");
                return;
            }
            fallbackManager.setLoading(false);
            ClientOverviewDTO clientOverviewDTO = result.getData();

            renderUI(clientOverviewDTO);
        });
        return result;
    }

    private Result<ClientOverviewDTO> handleOverviewException(Throwable throwable) {
        Platform.runLater(() ->
            fallbackManager.setErrorMessage("Failed to load client overview")
        );
        return new Result<>();
    }

    private void renderUI(ClientOverviewDTO clientOverviewDTO) {
        renderDates();
        renderLocation();
        renderEntityFlowPane("Supplied Products", clientOverviewDTO.getSuppliedProducts(), "Product");
        renderEntityFlowPane("Delivered from Factories", clientOverviewDTO.getDeliveredFromFactories(), "Factory");
        renderEntityFlowPane("Delivered from Warehouses", clientOverviewDTO.getDeliveredFromWarehouses(), "Warehouse");
    }

    private void renderDates() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy, hh:mm a");
        String formattedCreatedAt = client.getCreatedAt() != null ? client.getCreatedAt().format(dateTimeFormatter) : "None";
        String formattedUpdatedAt = client.getUpdatedAt() != null ? client.getUpdatedAt().format(dateTimeFormatter) : "None";
        renderField("Created At: ", formattedCreatedAt);
        renderField("Last Modified: ", formattedUpdatedAt);
    }

    private void renderLocation() {
        if (client.getLocation() == null) {
            return;
        }

        Label locationLabel = new Label("• Location");
        locationLabel.getStyleClass().setAll("general-label-large");
        detailsVBox.getChildren().add(locationLabel);

        renderField("Address: ", client.getLocation().getAddress());
        renderField("City: ", client.getLocation().getCity());
        renderField("State: ", client.getLocation().getState());
        renderField("Country: ", client.getLocation().getCountry());
        renderField("Zip Code: ", client.getLocation().getZipCode());
        renderField("Latitude: ", client.getLocation().getLatitude() != null ? client.getLocation().getLatitude().toString() : "");
        renderField("Longitude: ", client.getLocation().getLongitude() != null ? client.getLocation().getLongitude().toString() : "");
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
