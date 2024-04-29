package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.SceneManager;
import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.enums.InfoLevel;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.Objects;
import java.util.function.Consumer;

public class AccountSettingsController implements DataReceiver<UserSettings> {

    // Services
    private final AuthenticationService authenticationService;
    private final CommonViewsLoader commonViewsLoader;

    // State
    private UserSettings userSettings;

    // Listeners
    private RunnableConfirmDialogActionListener<String> confirmLogoutDialogListener;
    private RunnableConfirmDialogActionListener<String> confirmDeleteAccountDialogListener;

    // Controllers
    private GenericConfirmDialogController<String> confirmLogoutDialogController;
    private GenericConfirmDialogController<String> confirmDeleteAccountDialogController;

    // FXML
    @FXML
    private Button logoutButton;
    @FXML
    private Button deleteAccountButton;
    @FXML
    private StackPane confirmLogoutDialogContainer;
    @FXML
    private StackPane confirmDeleteAccountDialogContainer;

    // Icons
    private Image logoutIcon;

    @Inject
    public AccountSettingsController(AuthenticationService authenticationService,
                                     CommonViewsLoader commonViewsLoader) {
        this.authenticationService = authenticationService;
        this.commonViewsLoader = commonViewsLoader;
    }

    @Override
    public void setData(UserSettings data) {
        this.userSettings = data;

        confirmLogoutDialogController = commonViewsLoader.loadConfirmDialog(confirmDeleteAccountDialogContainer);
        toggleDialogVisibility(confirmDeleteAccountDialogContainer, false);

        confirmDeleteAccountDialogController = commonViewsLoader.loadConfirmDialog(confirmDeleteAccountDialogContainer);
        toggleDialogVisibility(confirmDeleteAccountDialogContainer, false);

        initializeIcons();
        initializeConfirmListeners();
        renderUI();
    }

    private void openConfirmLogoutDialog() {
        confirmLogoutDialogController.setData(null,
                new ConfirmDialogInput("Logout", "Are you sure you want to logout?", null)
        );
        toggleDialogVisibility(confirmLogoutDialogContainer, true);
    }

    private void openConfirmDeleteAccountDialog() {
        confirmDeleteAccountDialogController.setData(null,
                new ConfirmDialogInput("Delete Account", "Are you sure you want to delete your account? This action is irreversible.", null)
        );
        toggleDialogVisibility(confirmDeleteAccountDialogContainer, true);
    }

    private void initializeIcons() {
        logoutIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/right-from-bracket-solid.png")));
    }

    private void initializeConfirmListeners() {
        Consumer<String> onConfirmLogout = this::handleLogout;
        Runnable onCancelLogout = () -> toggleDialogVisibility(confirmLogoutDialogContainer, false);
        confirmLogoutDialogListener = new RunnableConfirmDialogActionListener<>(onConfirmLogout, onCancelLogout);
        confirmLogoutDialogController.setActionListener(confirmLogoutDialogListener);

        Consumer<String> onConfirmDeleteAccount = this::handleDeleteAccount;
        Runnable onCancelDeleteAccount = () -> toggleDialogVisibility(confirmDeleteAccountDialogContainer, false);
        confirmDeleteAccountDialogListener = new RunnableConfirmDialogActionListener<>(onConfirmDeleteAccount, onCancelDeleteAccount);
        confirmDeleteAccountDialogController.setActionListener(confirmDeleteAccountDialogListener);
    }

    private void toggleDialogVisibility(StackPane dialogContainer, boolean isVisible) {
        dialogContainer.setVisible(isVisible);
        dialogContainer.setManaged(isVisible);
    }

    private void renderUI() {
        logoutButton.setText("Logout");
        ImageView logoutIconView = new ImageView(logoutIcon);
        logoutIconView.setFitHeight(16);
        logoutIconView.setFitWidth(16);
        logoutButton.setGraphic(logoutIconView);
        logoutButton.setOnAction(event -> openConfirmLogoutDialog());

        deleteAccountButton.setText("Delete Account");
        deleteAccountButton.setOnAction(event -> openConfirmDeleteAccountDialog());

    }

    private void handleLogout(String userId) {
        authenticationService.logout();

        // Switch back to login scene
        try {
            SceneManager.loadLoginScene();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleDeleteAccount(String userId) {

    }
}
