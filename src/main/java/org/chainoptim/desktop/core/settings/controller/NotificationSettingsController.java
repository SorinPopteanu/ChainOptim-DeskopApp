package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.shared.util.DataReceiver;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
    private final ObservableList<String> changedSettings = FXCollections.observableArrayList();

    // Listeners
    @Setter
    private SettingsListener settingsListener;

    // Constants
    private static final List<String> notificationFeatures = List.of("Supplier Orders", "Client Orders", "Factory Inventory", "Warehouse Inventory");

    // FXML
    @FXML
    private VBox contentVBox;
    private final ToggleButton toggleOverallButton = new ToggleButton();
    private final Map<String, ToggleButton> featureToggleButtons = new HashMap<>();

    @Override
    public void setData(UserSettings userSettings) {
        initializeUI(userSettings);
    }

    public void cancelChanges(UserSettings originalUserSettings) {
        System.out.println("Canceling changes " + originalUserSettings);
        toggleOverallButton.setSelected(aggregateNotificationSettings(originalUserSettings));
        toggleOverallButton.setText(aggregateNotificationSettings(originalUserSettings) ? "On" : "Off");
        for (String feature : notificationFeatures) {
            ToggleButton toggleButton = featureToggleButtons.get(feature);
            toggleButton.setSelected(getFeatureSetting(originalUserSettings, feature));
            toggleButton.setText(getFeatureSetting(originalUserSettings, feature) ? "On" : "Off");
        }
        changedSettings.clear();
    }

    private void initializeUI(UserSettings userSettings) {
        contentVBox.setSpacing(10);

        renderOverallHBox(userSettings);

        for (String feature : notificationFeatures) {
            renderFeatureHBox(userSettings, feature);
        }

        changedSettings.addListener((ListChangeListener<String>) change ->
                settingsListener.handleSettingsChanged(!changedSettings.isEmpty()));
    }

    private void renderOverallHBox(UserSettings userSettings) {
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
        toggleOverallButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                handleToggleOverallButton(userSettings));
        overallHBox.getChildren().add(toggleOverallButton);

        contentVBox.getChildren().add(overallHBox);
    }

    private void renderFeatureHBox(UserSettings userSettings, String feature) {
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
        if (!changedSettings.contains(feature)) {
            changedSettings.add(feature);
        } else {
            changedSettings.remove(feature);
        }
        setFeatureSetting(userSettings, feature, isOn);
    }

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
