package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.shared.util.DataReceiver;

import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
    private ChangeListener<Boolean> hasChangedListener;
    private ChangeListener<Boolean> overallChangeListener;

    // Constants
    private static final List<String> notificationFeatures = List.of("Supplier Orders", "Client Orders", "Factory Inventory", "Warehouse Inventory");

    // FXML
    @FXML
    private VBox contentVBox;
    private JFXToggleButton overallToggleButton = new JFXToggleButton();
    private final Map<String, JFXToggleButton> featureToggleButtons = new HashMap<>();

    @Override
    public void setData(UserSettings userSettings) {
        this.userSettings = userSettings;
        initializeUI();
    }

    private void initializeUI() {
        contentVBox.getChildren().clear();
        contentVBox.setSpacing(12);

        cleanUpGlobalListeners();

        renderOverallHBox();

        for (String feature : notificationFeatures) {
            renderFeatureHBox(feature);
        }

        setUpGlobalListeners();
    }

    private void cleanUpGlobalListeners() {
        haveSettingsChanged.setValue(false);
        if (hasChangedListener != null) {
            haveSettingsChanged.removeListener(hasChangedListener);
        }
        if (overallChangeListener != null) {
            overallToggleButton.selectedProperty().removeListener(overallChangeListener);
        }
    }

    private void setUpGlobalListeners() {
        // Any change listener
        hasChangedListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (settingsListener != null) {
                settingsListener.handleSettingsChanged(newValue);
            }
        };
        haveSettingsChanged.addListener(hasChangedListener);

        // Overall change listener
        overallChangeListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            handleToggleOverallSwitch();
        };
        overallToggleButton.selectedProperty().addListener(overallChangeListener);
    }

    private void renderOverallHBox() {
        HBox overallHBox = new HBox();
        Label overallLabel = new Label("Overall");
        overallLabel.getStyleClass().add("settings-section-label");
        overallHBox.getChildren().add(overallLabel);

        Region region = new Region();
        overallHBox.getChildren().add(region);
        HBox.setHgrow(region, Priority.ALWAYS);

        overallToggleButton = new JFXToggleButton();
        boolean overallSetting = aggregateNotificationSettings();
        overallToggleButton.setSelected(overallSetting);
        styleToggleButton(overallToggleButton);
        overallHBox.getChildren().add(overallToggleButton);

        contentVBox.getChildren().add(overallHBox);
    }

    private void renderFeatureHBox(String feature) {
        HBox featureHBox = new HBox();
        featureHBox.setAlignment(Pos.CENTER_LEFT);
        Label featureLabel = new Label(feature);
        featureLabel.getStyleClass().add("settings-label");
        featureHBox.getChildren().add(featureLabel);

        Region region = new Region();
        featureHBox.getChildren().add(region);
        HBox.setHgrow(region, Priority.ALWAYS);

        JFXToggleButton toggleButton = new JFXToggleButton();
        boolean featureSetting = getFeatureSetting(feature);
        toggleButton.setSelected(featureSetting);
        styleToggleButton(toggleButton);
        toggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                handleToggleFeatureSwitch(featureToggleButtons.get(feature), feature, newValue));
        featureToggleButtons.put(feature, toggleButton);
        featureHBox.getChildren().add(toggleButton);

        contentVBox.getChildren().add(featureHBox);
    }

    private void handleToggleOverallSwitch() {
        boolean newState = overallToggleButton.isSelected();
        for (String feature : notificationFeatures) {
            JFXToggleButton featureToggleButton = featureToggleButtons.get(feature);
            handleToggleFeatureSwitch(featureToggleButton, feature, newState);
        }
    }

    private void handleToggleFeatureSwitch(JFXToggleButton featureToggleButton, String feature, Boolean isOn) {
        featureToggleButton.setSelected(isOn);
        haveSettingsChanged.setValue(true);
        System.out.println("Settings changed: " + haveSettingsChanged);
        setFeatureSetting(feature, isOn);
    }

    public void cancelChanges(UserSettings originalUserSettings) {
        overallToggleButton.selectedProperty().removeListener(overallChangeListener);
        setData(originalUserSettings);
        haveSettingsChanged.setValue(false);
    }

    // Utils
    private boolean aggregateNotificationSettings() {
        return userSettings.getNotificationSettings().isClientOrdersOn() &&
                userSettings.getNotificationSettings().isSupplierOrdersOn() &&
                userSettings.getNotificationSettings().isFactoryInventoryOn() &&
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

    private void setFeatureSetting(String feature, boolean isOn) {
        switch (feature) {
            case "Supplier Orders" -> userSettings.getNotificationSettings().setSupplierOrdersOn(isOn);
            case "Client Orders" -> userSettings.getNotificationSettings().setClientOrdersOn(isOn);
            case "Factory Inventory" -> userSettings.getNotificationSettings().setFactoryInventoryOn(isOn);
            case "Warehouse Inventory" -> userSettings.getNotificationSettings().setWarehouseInventoryOn(isOn);
            default -> {}
        }
    }

    private void styleToggleButton(JFXToggleButton toggleButton) {
        toggleButton.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-border-color: transparent;");
        toggleButton.setUnToggleColor(Color.valueOf("#cccccc"));
        toggleButton.setToggleColor(Color.valueOf("#006AEE"));
        toggleButton.setToggleLineColor(Color.valueOf("#337BEF"));
        toggleButton.setBorder(null);
        toggleButton.setDisableVisualFocus(true);
        toggleButton.setMinSize(50, 30);
    }
}
