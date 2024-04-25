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
    private ChangeListener<Boolean> notificationOverallChangeListener;

    // Constants
    private static final List<String> notificationFeatures = List.of("Supplier Orders", "Client Orders", "Factory Inventory", "Warehouse Inventory");

    // FXML
    @FXML
    private VBox contentVBox;
    private JFXToggleButton notificationOverallToggleButton = new JFXToggleButton();
    private final Map<String, JFXToggleButton> notificationFeatureToggleButtons = new HashMap<>();

    @Override
    public void setData(UserSettings userSettings) {
        this.userSettings = userSettings;
        initializeUI();
    }

    private void initializeUI() {
        contentVBox.getChildren().clear();
        contentVBox.setSpacing(12);

        renderOverallHBox();

        for (String feature : notificationFeatures) {
            renderFeatureHBox(feature);
        }

        setUpGlobalListeners();
    }

    private void renderOverallHBox() {
        HBox overallHBox = new HBox();
        Label overallLabel = new Label("Overall");
        overallLabel.getStyleClass().add("settings-section-label");
        overallHBox.getChildren().add(overallLabel);

        Region region = new Region();
        overallHBox.getChildren().add(region);
        HBox.setHgrow(region, Priority.ALWAYS);

        notificationOverallToggleButton = new JFXToggleButton();
        boolean overallSetting = aggregateNotificationSettings();
        notificationOverallToggleButton.setSelected(overallSetting);
        styleToggleButton(notificationOverallToggleButton);
        overallHBox.getChildren().add(notificationOverallToggleButton);

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
        boolean featureSetting = getNotificationFeatureSetting(feature);
        toggleButton.setSelected(featureSetting);
        styleToggleButton(toggleButton);
        toggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                handleToggleFeatureSwitch(notificationFeatureToggleButtons.get(feature), feature, newValue));
        notificationFeatureToggleButtons.put(feature, toggleButton);
        featureHBox.getChildren().add(toggleButton);

        contentVBox.getChildren().add(featureHBox);
    }

    private void handleToggleOverallSwitch(List<String> features) {
        boolean newState = notificationOverallToggleButton.isSelected();
        for (String feature : features) {
            JFXToggleButton featureToggleButton = notificationFeatureToggleButtons.get(feature);
            handleToggleFeatureSwitch(featureToggleButton, feature, newState);
        }
    }

    private void handleToggleFeatureSwitch(JFXToggleButton featureToggleButton, String feature, Boolean isOn) {
        featureToggleButton.setSelected(isOn);
        haveSettingsChanged.setValue(true);
        setNotificationFeatureSetting(feature, isOn);
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
        notificationOverallChangeListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            handleToggleOverallSwitch(notificationFeatures);
        };
        notificationOverallToggleButton.selectedProperty().addListener(notificationOverallChangeListener);
    }

    public void commitChanges(UserSettings newSettings) {
        // Temporarily remove listeners to prevent unpredictable behavior
        removeListeners();

        this.userSettings = newSettings;
        updateUI();

        reinstallListeners();
        haveSettingsChanged.setValue(false);
    }

    public void cancelChanges(UserSettings originalUserSettings) {
        // Temporarily remove listeners to prevent unpredictable behavior
        removeListeners();

        this.userSettings = originalUserSettings;
        updateUI();

        reinstallListeners();
        haveSettingsChanged.setValue(false);
    }

    private void reinstallListeners() {
        notificationOverallToggleButton.selectedProperty().addListener(notificationOverallChangeListener);
        haveSettingsChanged.addListener(hasChangedListener);
    }

    private void removeListeners() {
        notificationOverallToggleButton.selectedProperty().removeListener(notificationOverallChangeListener);
        haveSettingsChanged.removeListener(hasChangedListener);
    }

    private void updateUI() {
        boolean overallSetting = aggregateNotificationSettings();
        notificationOverallToggleButton.setSelected(overallSetting);

        for (Map.Entry<String, JFXToggleButton> entry : notificationFeatureToggleButtons.entrySet()) {
            String feature = entry.getKey();
            JFXToggleButton toggleButton = entry.getValue();
            boolean featureSetting = getNotificationFeatureSetting(feature);
            toggleButton.setSelected(featureSetting);
        }
    }

    // Utils
    private boolean aggregateNotificationSettings() {
        return userSettings.getNotificationSettings().isClientOrdersOn() &&
                userSettings.getNotificationSettings().isSupplierOrdersOn() &&
                userSettings.getNotificationSettings().isFactoryInventoryOn() &&
                userSettings.getNotificationSettings().isWarehouseInventoryOn();
    }

    private boolean getNotificationFeatureSetting(String feature) {
        return switch (feature) {
            case "Supplier Orders" -> userSettings.getNotificationSettings().isSupplierOrdersOn();
            case "Client Orders" -> userSettings.getNotificationSettings().isClientOrdersOn();
            case "Factory Inventory" -> userSettings.getNotificationSettings().isFactoryInventoryOn();
            case "Warehouse Inventory" -> userSettings.getNotificationSettings().isWarehouseInventoryOn();
            default -> false;
        };
    }

    private void setNotificationFeatureSetting(String feature, boolean isOn) {
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
