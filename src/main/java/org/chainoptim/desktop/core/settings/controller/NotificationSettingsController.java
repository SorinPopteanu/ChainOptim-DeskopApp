package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.shared.util.DataReceiver;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class NotificationSettingsController implements DataReceiver<UserSettings> {

    private UserSettings userSettings;

    private static final List<String> notificationFeatures = List.of("Supplier Orders", "Client Orders", "Factory Inventory", "Warehouse Inventory");

    @FXML
    private VBox contentVBox;
    @FXML
    private ToggleButton toggleOverallButton;
    private List<ToggleButton> featureToggleButtons;

    @Override
    public void setData(UserSettings data) {
        this.userSettings = data;

        initializeUI();
    }

    private void initializeUI() {
        toggleOverallButton.setText(aggregateNotificationSettings() ? "On" : "Off");
        toggleOverallButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                toggleOverallButton.setText("On");
                // Perform action when switch is turned on
            } else {
                toggleOverallButton.setText("Off");
                // Perform action when switch is turned off
            }
        });

        featureToggleButtons = List.of();
        for (String feature : notificationFeatures) {
            HBox featureHBox = new HBox();
            Label featureLabel = new Label(feature);
            featureLabel.getStyleClass().add("settings-label");
            featureHBox.getChildren().add(featureLabel);

            Region region = new Region();
            featureHBox.getChildren().add(region);
            HBox.setHgrow(region, Priority.ALWAYS);

            ToggleButton toggleButton = new ToggleButton();
            toggleButton.setSelected(getFeatureSetting(feature));
            toggleButton.getStyleClass().add("toggle-button");
            toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    toggleButton.setText("On");
                    // Perform action when switch is turned on
                } else {
                    toggleButton.setText("Off");
                    // Perform action when switch is turned off
                }
            });
            featureToggleButtons.add(toggleButton);
            featureHBox.getChildren().add(toggleButton);

            contentVBox.getChildren().add(featureHBox);
        }
    }

    private boolean aggregateNotificationSettings() {
        return userSettings.getNotificationSettings().isClientOrdersOn() ||
                userSettings.getNotificationSettings().isSupplierOrdersOn() ||
                userSettings.getNotificationSettings().isFactoryInventoryOn() ||
                userSettings.getNotificationSettings().isWarehouseInventoryOn();
    }

    private boolean getFeatureSetting(String feature) {
        return switch (feature) {
            case "Supplier Orders" -> userSettings.getNotificationSettings().isSupplierOrdersOn();
            case "Client Orders" -> userSettings.getNotificationSettings().isClientOrdersOn();
            case "Factory Inventory" -> userSettings.getNotificationSettings().isFactoryInventoryOn();
            case "Warehouse Inventory" -> userSettings.getNotificationSettings().isWarehouseInventoryOn();
            default -> false;
        };
    }
}
