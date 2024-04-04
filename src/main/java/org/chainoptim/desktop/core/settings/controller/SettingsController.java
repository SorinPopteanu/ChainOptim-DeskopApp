package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.context.TenantSettingsContext;
import org.chainoptim.desktop.core.settings.dto.UpdateUserSettingsDTO;
import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.core.settings.service.UserSettingsService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class SettingsController implements Initializable, SettingsListener {

    // Services
    private final UserSettingsService userSettingsService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    // Controllers
    private NotificationSettingsController notificationSettingsController;

    // State
    private User currentUser;
    private UserSettings userSettings;

    @FXML
    private StackPane fallbackContainer;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab generalTab;
    @FXML
    private Tab accountTab;
    @FXML
    private Tab notificationTab;

    @FXML
    private Label usernameLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    @Inject
    public SettingsController(UserSettingsService userSettingsService,
                              FXMLLoaderService fxmlLoaderService,
                              ControllerFactory controllerFactory,
                              FallbackManager fallbackManager) {
        this.userSettingsService = userSettingsService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
        setupListeners();

        currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) return;

        usernameLabel.setText(currentUser.getUsername());
        handleSettingsChanged(false);

        userSettings = TenantSettingsContext.getCurrentUserSettings();
        if (userSettings == null) {
            loadUserSettings(currentUser.getId());
        } else {
            loadTabContent(generalTab, "/org/chainoptim/desktop/core/settings/GeneralSettingsView.fxml", userSettings);
        }
    }

    private void loadFallbackManager() {
        // Load view into fallbackContainer
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void setupListeners() {
        generalTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && generalTab.getContent() == null) {
                loadTabContent(generalTab, "/org/chainoptim/desktop/core/settings/GeneralSettingsView.fxml", this.userSettings);
            }
        });
        accountTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && accountTab.getContent() == null) {
                loadTabContent(accountTab, "/org/chainoptim/desktop/core/settings/AccountSettingsView.fxml", this.userSettings);
            }
        });
        notificationTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && notificationTab.getContent() == null) {
                loadTabContent(notificationTab, "/org/chainoptim/desktop/core/settings/NotificationSettingsView.fxml", this.userSettings);
            }
        });

        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            tabPane.setVisible(newValue);
            tabPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadTabContent(Tab tab, String fxmlFilepath, UserSettings userSettings) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilepath));
            loader.setControllerFactory(controllerFactory::createController);
            Node content = loader.load();
            DataReceiver<UserSettings> controller = loader.getController();
            controller.setData(userSettings);
            if (controller instanceof NotificationSettingsController notificationController) {
                notificationSettingsController = notificationController;
                notificationSettingsController.setSettingsListener(this);
            }
            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUserSettings(String userId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        userSettingsService.getUserSettings(userId)
                .thenApply(this::handleUserSettingsResponse)
                .exceptionally(this::handleUserSettingsException);
    }

    private Optional<UserSettings> handleUserSettingsResponse(Optional<UserSettings> userSettingsOptional) {
        Platform.runLater(() -> {
            if (userSettingsOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load user settings.");
                return;
            }

            userSettings = userSettingsOptional.get();
            fallbackManager.setLoading(false);

            loadTabContent(generalTab, "/org/chainoptim/desktop/core/settings/GeneralSettingsView.fxml", userSettings);
        });

        return userSettingsOptional;
    }

    private Optional<UserSettings> handleUserSettingsException(Throwable ex) {
        fallbackManager.setErrorMessage("Failed to load user settings.");
        return Optional.empty();
    }

    @Override
    public void handleSettingsChanged(boolean haveChanged) {
        saveButton.setDisable(!haveChanged);
        saveButton.getStyleClass().setAll(haveChanged ? "standard-write-button" : "standard-write-button-disabled");
        cancelButton.setVisible(haveChanged);
        cancelButton.setManaged(haveChanged);
    }

    @FXML
    public void handleSave() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        UpdateUserSettingsDTO userSettingsDTO = new UpdateUserSettingsDTO(userSettings.getId(), userSettings.getUserId(), userSettings.getNotificationSettings());

        // Save the user settings
        userSettingsService.saveUserSettings(userSettingsDTO)
                .thenApply(this::handleSaveResponse)
                .exceptionally(this::handleSaveException);
    }

    private Optional<UserSettings> handleSaveResponse(Optional<UserSettings> userSettingsOptional) {
        Platform.runLater(() -> {
            if (userSettingsOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to save user settings.");
                return;
            }
            userSettings = userSettingsOptional.get();
            TenantSettingsContext.setCurrentUserSettings(userSettings);

            fallbackManager.setLoading(false);
            handleSettingsChanged(false);
        });

        return userSettingsOptional;
    }

    private Optional<UserSettings> handleSaveException(Throwable ex) {
        fallbackManager.setErrorMessage("Failed to save user settings.");
        return Optional.empty();
    }

    @FXML
    private void handleCancel() {
        // Reselect based on original settings
        notificationSettingsController.cancelChanges(TenantSettingsContext.getCurrentUserSettings());
        handleSettingsChanged(false);
    }
}
