package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.shared.enums.Feature;
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

import java.util.*;

public class NotificationSettingsController implements DataReceiver<UserSettings> {

    // State
    private UserSettings userSettings;
    private boolean areNotificationsWithinPlan;
    private boolean areEmailsWithinPlan;
    private final BooleanProperty haveSettingsChanged = new SimpleBooleanProperty(false);

    // Listeners
    @Setter
    private SettingsListener settingsListener;
    private ChangeListener<Boolean> hasChangedListener;
    private ChangeListener<Boolean> notificationOverallChangeListener;
    private ChangeListener<Boolean> emailOverallChangeListener;

    // Constants
    private static final List<Feature> notificationFeatures = List.of(Feature.SUPPLIER_ORDER, Feature.CLIENT_ORDER, Feature.FACTORY_INVENTORY, Feature.WAREHOUSE_INVENTORY);
    private static final List<Feature> emailFeatures = List.of(Feature.SUPPLIER_ORDER, Feature.CLIENT_ORDER, Feature.FACTORY_INVENTORY, Feature.WAREHOUSE_INVENTORY);
    private static final String NOTIFICATIONS = "Notifications";
    private static final String EMAILS = "Emails";
    
    // FXML
    @FXML
    private VBox contentVBox;
    private final JFXToggleButton notificationOverallToggleButton = new JFXToggleButton();
    private final Map<Feature, JFXToggleButton> notificationFeatureToggleButtons = new EnumMap<>(Feature.class);
    private final JFXToggleButton emailOverallToggleButton = new JFXToggleButton();
    private final Map<Feature, JFXToggleButton> emailFeatureToggleButtons = new EnumMap<>(Feature.class);

    @Override
    public void setData(UserSettings userSettings) {
        this.userSettings = userSettings;
        this.areNotificationsWithinPlan = TenantContext.getCurrentUser().getOrganization().getSubscriptionPlan().isCustomNotificationsOn();
        this.areEmailsWithinPlan = TenantContext.getCurrentUser().getOrganization().getSubscriptionPlan().isEmailNotificationsOn();

        initializeUI();
    }

    private void initializeUI() {
        contentVBox.getChildren().clear();
        contentVBox.setSpacing(12);

        renderOverallHBox(NOTIFICATIONS);

        for (Feature feature : notificationFeatures) {
            renderFeatureHBox(feature, NOTIFICATIONS);
        }

        Region region = new Region();
        region.setMinHeight(8);
        contentVBox.getChildren().add(region);

        renderOverallHBox(EMAILS);

        for (Feature feature : emailFeatures) {
            renderFeatureHBox(feature, EMAILS);
        }

        setUpGlobalListeners();
    }

    private void renderOverallHBox(String type) {
        HBox overallHBox = new HBox();
        Label overallLabel = new Label(type);
        overallLabel.getStyleClass().add("settings-section-label");
        overallHBox.getChildren().add(overallLabel);

        Region region = new Region();
        overallHBox.getChildren().add(region);
        HBox.setHgrow(region, Priority.ALWAYS);

        if (Objects.equals(type, NOTIFICATIONS)) {
            boolean overallSetting = aggregateNotificationSettings();
            notificationOverallToggleButton.setSelected(overallSetting);
            notificationOverallToggleButton.setDisable(!areNotificationsWithinPlan);
            styleToggleButton(notificationOverallToggleButton);
            overallHBox.getChildren().add(notificationOverallToggleButton);
        } else if (EMAILS.equals(type)) {
            boolean overallSetting = aggregateEmailSettings();
            emailOverallToggleButton.setSelected(overallSetting);
            emailOverallToggleButton.setDisable(!areEmailsWithinPlan);
            styleToggleButton(emailOverallToggleButton);
            overallHBox.getChildren().add(emailOverallToggleButton);
        }

        contentVBox.getChildren().add(overallHBox);
    }

    private void renderFeatureHBox(Feature feature, String type) {
        HBox featureHBox = new HBox();
        featureHBox.setAlignment(Pos.CENTER_LEFT);
        Label featureLabel = new Label(feature.toString());
        featureLabel.getStyleClass().add("settings-label");
        featureHBox.getChildren().add(featureLabel);

        Region region = new Region();
        featureHBox.getChildren().add(region);
        HBox.setHgrow(region, Priority.ALWAYS);

        JFXToggleButton toggleButton = new JFXToggleButton();
        styleToggleButton(toggleButton);
        if (Objects.equals(type, NOTIFICATIONS)) {
            boolean featureSetting = getNotificationFeatureSetting(feature);
            toggleButton.setSelected(featureSetting);
            toggleButton.setDisable(!areNotificationsWithinPlan);
            toggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                    handleToggleFeatureSwitch(notificationFeatureToggleButtons.get(feature), feature, newValue, type));
            notificationFeatureToggleButtons.put(feature, toggleButton);
        } else if (EMAILS.equals(type)) {
            boolean featureSetting = getEmailFeatureSetting(feature);
            toggleButton.setDisable(!areEmailsWithinPlan);
            toggleButton.setSelected(featureSetting);
            toggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                    handleToggleFeatureSwitch(emailFeatureToggleButtons.get(feature), feature, newValue, type));
            emailFeatureToggleButtons.put(feature, toggleButton);
        }
        featureHBox.getChildren().add(toggleButton);

        contentVBox.getChildren().add(featureHBox);
    }

    private void handleToggleOverallSwitch(String type) {
        if (Objects.equals(type, NOTIFICATIONS)) {
            boolean newState = notificationOverallToggleButton.isSelected();
            for (Feature feature : notificationFeatures) {
                JFXToggleButton featureToggleButton = notificationFeatureToggleButtons.get(feature);
                handleToggleFeatureSwitch(featureToggleButton, feature, newState, type);
            }
        } else if (EMAILS.equals(type)) {
            boolean newState = emailOverallToggleButton.isSelected();
            for (Feature feature : emailFeatures) {
                JFXToggleButton featureToggleButton = emailFeatureToggleButtons.get(feature);
                handleToggleFeatureSwitch(featureToggleButton, feature, newState, type);
            }
        }
    }

    private void handleToggleFeatureSwitch(JFXToggleButton featureToggleButton, Feature feature, Boolean isOn, String type) {
        featureToggleButton.setSelected(isOn);
        haveSettingsChanged.setValue(true);
        if (Objects.equals(type, NOTIFICATIONS)) {
            setNotificationFeatureSetting(feature, isOn);
        } else if (EMAILS.equals(type)) {
            setEmailFeatureSetting(feature, isOn);
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
        notificationOverallChangeListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            handleToggleOverallSwitch(NOTIFICATIONS);
        };
        notificationOverallToggleButton.selectedProperty().addListener(notificationOverallChangeListener);

        emailOverallChangeListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            handleToggleOverallSwitch(EMAILS);
        };
        emailOverallToggleButton.selectedProperty().addListener(emailOverallChangeListener);
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
        emailOverallToggleButton.selectedProperty().addListener(emailOverallChangeListener);
        haveSettingsChanged.addListener(hasChangedListener);
    }

    private void removeListeners() {
        notificationOverallToggleButton.selectedProperty().removeListener(notificationOverallChangeListener);
        emailOverallToggleButton.selectedProperty().removeListener(emailOverallChangeListener);
        haveSettingsChanged.removeListener(hasChangedListener);
    }

    private void updateUI() {
        boolean overallSetting = aggregateNotificationSettings();
        notificationOverallToggleButton.setSelected(overallSetting);

        for (Map.Entry<Feature, JFXToggleButton> entry : notificationFeatureToggleButtons.entrySet()) {
            Feature feature = entry.getKey();
            JFXToggleButton toggleButton = entry.getValue();
            boolean featureSetting = getNotificationFeatureSetting(feature);
            toggleButton.setSelected(featureSetting);
        }

        boolean emailOverallSetting = aggregateEmailSettings();
        emailOverallToggleButton.setSelected(emailOverallSetting);

        for (Map.Entry<Feature, JFXToggleButton> entry : emailFeatureToggleButtons.entrySet()) {
            Feature feature = entry.getKey();
            JFXToggleButton toggleButton = entry.getValue();
            boolean featureSetting = getEmailFeatureSetting(feature);
            toggleButton.setSelected(featureSetting);
        }
    }

    // Utils
    private boolean aggregateNotificationSettings() {
        return userSettings.getNotificationSettings().isSupplierOrdersOn() &&
                userSettings.getNotificationSettings().isClientOrdersOn() &&
                userSettings.getNotificationSettings().isFactoryInventoryOn() &&
                userSettings.getNotificationSettings().isWarehouseInventoryOn();
    }

    private boolean aggregateEmailSettings() {
        return userSettings.getNotificationSettings().isEmailSupplierOrdersOn() &&
                userSettings.getNotificationSettings().isEmailClientOrdersOn() &&
                userSettings.getNotificationSettings().isEmailFactoryInventoryOn() &&
                userSettings.getNotificationSettings().isEmailWarehouseInventoryOn();
    }

    private boolean getNotificationFeatureSetting(Feature feature) {
        return switch (feature) {
            case SUPPLIER_ORDER -> userSettings.getNotificationSettings().isSupplierOrdersOn();
            case CLIENT_ORDER -> userSettings.getNotificationSettings().isClientOrdersOn();
            case FACTORY_INVENTORY -> userSettings.getNotificationSettings().isFactoryInventoryOn();
            case WAREHOUSE_INVENTORY -> userSettings.getNotificationSettings().isWarehouseInventoryOn();
            default -> false;
        };
    }

    private boolean getEmailFeatureSetting(Feature feature) {
        return switch (feature) {
            case SUPPLIER_ORDER -> userSettings.getNotificationSettings().isEmailSupplierOrdersOn();
            case CLIENT_ORDER -> userSettings.getNotificationSettings().isEmailClientOrdersOn();
            case FACTORY_INVENTORY -> userSettings.getNotificationSettings().isEmailFactoryInventoryOn();
            case WAREHOUSE_INVENTORY -> userSettings.getNotificationSettings().isEmailWarehouseInventoryOn();
            default -> false;
        };
    }

    private void setNotificationFeatureSetting(Feature feature, boolean isOn) {
        switch (feature) {
            case SUPPLIER_ORDER -> userSettings.getNotificationSettings().setSupplierOrdersOn(isOn);
            case CLIENT_ORDER -> userSettings.getNotificationSettings().setClientOrdersOn(isOn);
            case FACTORY_INVENTORY -> userSettings.getNotificationSettings().setFactoryInventoryOn(isOn);
            case WAREHOUSE_INVENTORY -> userSettings.getNotificationSettings().setWarehouseInventoryOn(isOn);
            default -> throw new IllegalStateException("Unexpected value: " + feature);
        }
    }

    private void setEmailFeatureSetting(Feature feature, boolean isOn) {
        switch (feature) {
            case SUPPLIER_ORDER -> userSettings.getNotificationSettings().setEmailSupplierOrdersOn(isOn);
            case CLIENT_ORDER -> userSettings.getNotificationSettings().setEmailClientOrdersOn(isOn);
            case FACTORY_INVENTORY -> userSettings.getNotificationSettings().setEmailFactoryInventoryOn(isOn);
            case WAREHOUSE_INVENTORY -> userSettings.getNotificationSettings().setEmailWarehouseInventoryOn(isOn);
            default -> throw new IllegalStateException("Unexpected value: " + feature);
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
