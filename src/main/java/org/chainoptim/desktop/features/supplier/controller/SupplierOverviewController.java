package org.chainoptim.desktop.features.supplier.controller;

import org.chainoptim.desktop.features.supplier.dto.SupplierOverviewDTO;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.service.SupplierService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class SupplierOverviewController implements DataReceiver<Supplier> {

    private final SupplierService supplierService;

    private FallbackManager fallbackManager;

    private Supplier supplier;

    @FXML
    private VBox locationVBox;

    @Inject
    public SupplierOverviewController(SupplierService supplierService,
                                      FallbackManager fallbackManager) {
        this.supplierService = supplierService;
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

            SupplierOverviewDTO supplierOverviewDTO = result.getData();
            System.out.println("Supplier Overview DTO: " + supplierOverviewDTO);

            renderLocation();
        });
        return result;
    }

    private Result<SupplierOverviewDTO> handleOverviewException(Throwable throwable) {
        Platform.runLater(() ->
            fallbackManager.setErrorMessage("Failed to load supplier overview")
        );
        return new Result<>();
    }

    private void renderLocation() {
        if (supplier.getLocation() == null) {
            return;
        }

        renderLocationField("Address: ", supplier.getLocation().getAddress());
        renderLocationField("City: ", supplier.getLocation().getCity());
        renderLocationField("State: ", supplier.getLocation().getState());
        renderLocationField("Country: ", supplier.getLocation().getCountry());
        renderLocationField("Zip Code: ", supplier.getLocation().getZipCode());
        renderLocationField("Latitude: ", supplier.getLocation().getLatitude() != null ? supplier.getLocation().getLatitude().toString() : "");
        renderLocationField("Longitude: ", supplier.getLocation().getLongitude() != null ? supplier.getLocation().getLongitude().toString() : "");
    }

    private void renderLocationField(String field, String fieldValue) {
        HBox fieldHBox = new HBox(8);
        fieldHBox.setAlignment(Pos.CENTER_LEFT);
        Label countryLabel = new Label(field);
        countryLabel.getStyleClass().setAll("general-label-medium-large");

        Label countryValueLabel = new Label();
        countryValueLabel.getStyleClass().setAll("general-label");
        countryValueLabel.setText(fieldValue);

        if (fieldValue != null) {
            fieldHBox.getChildren().addAll(countryLabel, countryValueLabel);
            locationVBox.getChildren().add(fieldHBox);
        }
    }


}
