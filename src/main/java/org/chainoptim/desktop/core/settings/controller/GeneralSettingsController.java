package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.shared.common.uielements.settings.EnumSelector;
import org.chainoptim.desktop.shared.enums.InfoLevel;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.version.VersionCheckerService;

import com.google.inject.Inject;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.Setter;

public class GeneralSettingsController implements DataReceiver<UserSettings> {

    // Services
    private final VersionCheckerService versionCheckerService;

    // Listeners
    @Setter
    private SettingsListener settingsListener;
    private ChangeListener<InfoLevel> overallChangeListener;

    // State
    private UserSettings userSettings;

    // FXML
    @FXML
    private EnumSelector<InfoLevel> infoLevelSelector;

    @Inject
    public GeneralSettingsController(VersionCheckerService versionCheckerService) {
        this.versionCheckerService = versionCheckerService;
    }

    @Override
    public void setData(UserSettings data) {
        this.userSettings = data;

        infoLevelSelector.getChildren().clear();
        infoLevelSelector.initializeSelector(InfoLevel.class, userSettings.getGeneralSettings().getInfoLevel());

        setUpListeners();
        checkSoftwareVersion();
    }

    private void setUpListeners() {
        if (overallChangeListener != null) {
            infoLevelSelector.getValueProperty().removeListener(overallChangeListener);
        }
        overallChangeListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                userSettings.getGeneralSettings().setInfoLevel(newValue);
                settingsListener.handleSettingsChanged(true);
            }
        };
        infoLevelSelector.getValueProperty().addListener(overallChangeListener);
    }

    private void checkSoftwareVersion() {

    }

    public void cancelChanges(UserSettings originalUserSettings) {
        infoLevelSelector.selectValue(originalUserSettings.getGeneralSettings().getInfoLevel(), InfoLevel.class);
    }

}
