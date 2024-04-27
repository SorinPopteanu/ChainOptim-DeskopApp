package org.chainoptim.desktop.core.main.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import javafx.fxml.FXML;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.context.TenantSettingsContext;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.notification.controller.NotificationManager;
import org.chainoptim.desktop.core.notification.model.Notification;
import org.chainoptim.desktop.core.notification.service.NotificationWebSocketClient;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.settings.service.UserSettingsService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.user.service.UserService;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Consumer;
import static org.chainoptim.desktop.core.organization.model.Organization.SubscriptionPlanTier.PRO;

/**
 * Root controller managing the currently displayed main content
 * through sidebarController and navigationService.
 * It is also responsible for loading the current user details, user settings,
 * and starting a WebSocket connection for real-time notifications.
 */
public class AppController {

    private final NavigationService navigationService;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final UserSettingsService userSettingsService;
    private final NotificationManager notificationManager;

    private final SidebarController sidebarController;

    @FXML
    private StackPane contentArea;
    @FXML
    private BorderPane appPane;
    @FXML
    private StackPane startUpArea;

    @Inject
    public AppController(NavigationService navigationService,
                         AuthenticationService authenticationService,
                         UserService userService,
                         UserSettingsService userSettingsService,
                         NotificationManager notificationManager,
                         SidebarController sidebarController) {
        this.navigationService = navigationService;
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.userSettingsService = userSettingsService;
        this.notificationManager = notificationManager;
        this.sidebarController = sidebarController;
    }

    public void initialize() {
        navigationService.setMainContentArea(contentArea);
        sidebarController.setNavigationService(navigationService);
        loaadStartUpView();

        // Load user from JWT token
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser != null) return;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return;

        authenticationService.getUsernameFromJWTToken(jwtToken).ifPresent(this::fetchAndSetUser);
    }

    private void loaadStartUpView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chainoptim/desktop/core/main/StartUpView.fxml"));
            loader.setControllerFactory(MainApplication.injector::getInstance);
            startUpArea.getChildren().add(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchAndSetUser(String username) {
        userService.getUserByUsername(username)
                .thenApply(this::handleUserResponse)
                .exceptionally(ex -> new Result<>());
    }

    private Result<User> handleUserResponse(Result<User> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                return;
            }

            User user = result.getData();

            user.getOrganization().setSubscriptionPlanTier(PRO);

            // Set user to TenantContext for reuse throughout the app
            TenantContext.setCurrentUser(user);

            // Fetch user settings and set them to TenantSettingsContext
            userSettingsService.getUserSettings(user.getId())
                    .thenAccept(result1 -> {
                        if (result1.getError() != null) return;
                        TenantSettingsContext.setCurrentUserSettings(result1.getData());
                        startUpArea.setVisible(false);
                        appPane.setVisible(true);
                    });

            // Start WebSocket connection
            startWebSocket(user);
        });

        return result;
    }

    private void startWebSocket(User user) {
        String userIdParam = "?userId=" + user.getId();
        System.out.println("Connecting to WebSocket with userId: " + user.getId());

        try {
            URI serverUri = new URI("ws://localhost:8080/ws" + userIdParam);
            Consumer<Notification> addMessageToUI = notificationManager::showNotification;
            NotificationWebSocketClient client = new NotificationWebSocketClient(serverUri, addMessageToUI);
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
