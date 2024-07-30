package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.shared.common.uielements.settings.EnumSelector;
import org.chainoptim.desktop.shared.enums.InfoLevel;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.version.CheckVersionResponse;
import org.chainoptim.desktop.shared.version.VersionCheckerService;
import org.chainoptim.desktop.shared.version.VersionManager;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private Label versionLabel;
    @FXML
    private Label latestVersionLabel;
    @FXML
    private Button checkForUpdatesButton;
    @FXML
    private Label updateStatusLabel;
    @FXML
    private Button updateButton;
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

        String currentVersion = VersionManager.getCurrentVersion();
        versionLabel.setText(currentVersion);
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

    @FXML
    private void checkForUpdates() {
        String currentVersion = VersionManager.getCurrentVersion();

        versionCheckerService.checkVersion(currentVersion)
                .thenApply(this::handleVersionCheckResponse)
                .exceptionally(this::handleVersionCheckException);
    }

    private Result<CheckVersionResponse> handleVersionCheckResponse(Result<CheckVersionResponse> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                return;
            }

            CheckVersionResponse checkVersionResponse = result.getData();
            latestVersionLabel.setText(checkVersionResponse.getLatestVersion());
            latestVersionLabel.setVisible(true);
            updateStatusLabel.setVisible(true);
            if (checkVersionResponse.isUpdateAvailable()) {
                updateStatusLabel.setText("Update available");
                updateButton.setVisible(true);
                System.out.println("Update available");
            } else {
                updateStatusLabel.setText("No update available");
                System.out.println("No update available");
            }
        });
        return result;
    }

    private Result<CheckVersionResponse> handleVersionCheckException(Throwable throwable) {
        Platform.runLater(() -> {
            System.out.println("Error checking version: " + throwable.getMessage());
        });
        return new Result<>();
    }

    public void cancelChanges(UserSettings originalUserSettings) {
        infoLevelSelector.selectValue(originalUserSettings.getGeneralSettings().getInfoLevel(), InfoLevel.class);
    }

    @FXML
    private void updateToLatest() {
        System.out.println("Updating to latest version");
    }

}
