package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.shared.common.uielements.settings.EnumSelector;
import org.chainoptim.desktop.shared.enums.InfoLevel;
import org.chainoptim.desktop.shared.util.DataReceiver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import lombok.Setter;

public class GeneralSettingsController implements DataReceiver<UserSettings> {

    // Listeners
    @Setter
    private SettingsListener settingsListener;

    // State
    private UserSettings userSettings;
    private final ObservableList<String> changedSettings = FXCollections.observableArrayList();

    // FXML
    @FXML
    private EnumSelector<InfoLevel> infoLevelSelector;

    @Override
    public void setData(UserSettings data) {
        this.userSettings = data;

        infoLevelSelector.initializeSelector(InfoLevel.class, userSettings.getGeneralSettings().getInfoLevel());

        setUpListeners();
    }

    private void setUpListeners() {
        infoLevelSelector.getValueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                userSettings.getGeneralSettings().setInfoLevel(newValue);
                changedSettings.add("infoLevel");
                settingsListener.handleSettingsChanged(true);
            }
            System.out.println("Changed settings: " + changedSettings);
        });
    }

    public void cancelChanges(UserSettings originalUserSettings) {
        infoLevelSelector.selectValue(originalUserSettings.getGeneralSettings().getInfoLevel());
        changedSettings.clear();
    }
}
