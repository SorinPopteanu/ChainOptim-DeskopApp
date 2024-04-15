package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.shared.util.DataReceiver;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationSettingsController implements DataReceiver<UserSettings> {

    // State
    private UserSettings userSettings;
    private final BooleanProperty haveSettingsChanged = new SimpleBooleanProperty(false);

    // Listeners
    @Setter
    private SettingsListener settingsListener;
    private ChangeListener<Boolean> overallChangeListener;

    // Constants
    private static final List<String> notificationFeatures = List.of("Supplier Orders", "Client Orders", "Factory Inventory", "Warehouse Inventory");

    // FXML
    @FXML
    private VBox contentVBox;
    private final ToggleButton toggleOverallButton = new ToggleButton();
    private final Map<String, ToggleButton> featureToggleButtons = new HashMap<>();

    @Override
    public void setData(UserSettings userSettings) {
        this.userSettings = userSettings;
        initializeUI();
    }

    private void initializeUI() {
        contentVBox.getChildren().clear();
        contentVBox.setSpacing(10);

        renderOverallHBox();

        for (String feature : notificationFeatures) {
            renderFeatureHBox(feature);
        }

        haveSettingsChanged.addListener(change ->
                settingsListener.handleSettingsChanged(haveSettingsChanged.getValue()));
    }

    private void renderOverallHBox() {
        HBox overallHBox = new HBox();
        Label overallLabel = new Label("Overall");
        overallLabel.getStyleClass().add("settings-section-label");
        overallHBox.getChildren().add(overallLabel);

        Region region = new Region();
        overallHBox.getChildren().add(region);
        HBox.setHgrow(region, Priority.ALWAYS);

        toggleOverallButton.getStyleClass().setAll("toggle-button");
        boolean overallSetting = aggregateNotificationSettings(userSettings);
        toggleOverallButton.setText(overallSetting ? "On" : "Off");
        toggleOverallButton.setSelected(overallSetting);
        overallChangeListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            handleToggleOverallButton(userSettings);
        };

        // Add the listener to the toggle button
        toggleOverallButton.selectedProperty().addListener(overallChangeListener);
        overallHBox.getChildren().add(toggleOverallButton);

        contentVBox.getChildren().add(overallHBox);
    }

    private void renderFeatureHBox(String feature) {
        HBox featureHBox = new HBox();
        Label featureLabel = new Label(feature);
        featureLabel.getStyleClass().add("settings-label");
        featureHBox.getChildren().add(featureLabel);

        Region region = new Region();
        featureHBox.getChildren().add(region);
        HBox.setHgrow(region, Priority.ALWAYS);

        ToggleButton toggleButton = new ToggleButton();
        boolean featureSetting = getFeatureSetting(userSettings, feature);
        toggleButton.setText(featureSetting ? "On" : "Off");
        toggleButton.setSelected(featureSetting);
        toggleButton.getStyleClass().setAll("toggle-button");
        toggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                handleToggleFeatureButton(userSettings, toggleButton, feature, toggleButton.isSelected()));
        featureToggleButtons.put(feature, toggleButton);
        featureHBox.getChildren().add(toggleButton);

        contentVBox.getChildren().add(featureHBox);
    }

    private void handleToggleOverallButton(UserSettings userSettings) {
        if (toggleOverallButton.isSelected()) {
            toggleOverallButton.setText("On");
        } else {
            toggleOverallButton.setText("Off");
        }
        for (String feature : notificationFeatures) {
            ToggleButton toggleButton = featureToggleButtons.get(feature);
            handleToggleFeatureButton(userSettings, toggleButton, feature, toggleOverallButton.isSelected());
        }
    }

    private void handleToggleFeatureButton(UserSettings userSettings, ToggleButton toggleButton, String feature, Boolean isOn) {
        toggleButton.setSelected(isOn);
        toggleButton.setText(Boolean.TRUE.equals(isOn) ? "On" : "Off");
        haveSettingsChanged.setValue(true);
        setFeatureSetting(userSettings, feature, isOn);
    }

    public void cancelChanges(UserSettings originalUserSettings) {
        toggleOverallButton.selectedProperty().removeListener(overallChangeListener);
        setData(originalUserSettings);
        haveSettingsChanged.setValue(false);
    }

    // Utils
    private boolean aggregateNotificationSettings(UserSettings userSettings) {
        return userSettings.getNotificationSettings().isClientOrdersOn() ||
                userSettings.getNotificationSettings().isSupplierOrdersOn() ||
                userSettings.getNotificationSettings().isFactoryInventoryOn() ||
                userSettings.getNotificationSettings().isWarehouseInventoryOn();
    }

    private boolean getFeatureSetting(UserSettings userSettings, String feature) {
        return switch (feature) {
            case "Supplier Orders" -> userSettings.getNotificationSettings().isSupplierOrdersOn();
            case "Client Orders" -> userSettings.getNotificationSettings().isClientOrdersOn();
            case "Factory Inventory" -> userSettings.getNotificationSettings().isFactoryInventoryOn();
            case "Warehouse Inventory" -> userSettings.getNotificationSettings().isWarehouseInventoryOn();
            default -> false;
        };
    }

    private void setFeatureSetting(UserSettings userSettings, String feature, boolean isOn) {
        switch (feature) {
            case "Supplier Orders" -> userSettings.getNotificationSettings().setSupplierOrdersOn(isOn);
            case "Client Orders" -> userSettings.getNotificationSettings().setClientOrdersOn(isOn);
            case "Factory Inventory" -> userSettings.getNotificationSettings().setFactoryInventoryOn(isOn);
            case "Warehouse Inventory" -> userSettings.getNotificationSettings().setWarehouseInventoryOn(isOn);
            default -> {}
        }
    }
}
