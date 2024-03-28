package org.chainoptim.desktop.core.overview.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.overview.model.SupplyChainSnapshot;
import org.chainoptim.desktop.core.overview.service.SupplyChainSnapshotService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.core.user.service.UserService;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class OverviewController implements Initializable {

    // Services
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final SupplyChainSnapshotService supplyChainSnapshotService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    // State
    private SupplyChainSnapshot snapshot;

    // FXML
    @FXML
    private StackPane contentContainer;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private Label factoriesCountLabel;

    @Inject
    public OverviewController(AuthenticationService authenticationService,
                              UserService userService,
                              SupplyChainSnapshotService supplyChainSnapshotService,
                              FXMLLoaderService fxmlLoaderService,
                              ControllerFactory controllerFactory,
                              FallbackManager fallbackManager) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.supplyChainSnapshotService = supplyChainSnapshotService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser != null) return;

        String jwtToken = TokenManager.getToken();
        if (jwtToken == null) return; // Future: Switch to Login Scene

        loadFallbackManager();
        setUpListeners();

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        authenticationService.getUsernameFromJWTToken(jwtToken).ifPresent(this::fetchAndSetUser);
    }

    private void loadFallbackManager() {
        // Load view into fallbackContainer
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void setUpListeners() {
        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            contentContainer.setVisible(newValue);
            contentContainer.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void fetchAndSetUser(String username) {
        userService.getUserByUsername(username)
                .thenApply(this::handleUserResponse)
                .exceptionally(ex -> {
                    Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load user."));
                    return Optional.empty();
                });
    }

    private Optional<User> handleUserResponse(Optional<User> userOptional) {
        Platform.runLater(() -> {
            if (userOptional.isEmpty()) {
                return;
            }
            User user = userOptional.get();

            // Set user to TenantContext for reuse throughout the app
            TenantContext.setCurrentUser(user);

            Integer organizationId = user.getOrganization().getId();
            if (organizationId == null) return;

            // Fetch Supply Chain snapshot
            supplyChainSnapshotService.getSupplyChainSnapshot(organizationId)
                    .thenApply(this::handleSupplyChainSnapshotResponse)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supply chain snapshot."));
                        return Optional.empty();
                    });

        });
        return userOptional;
    }

    private Optional<SupplyChainSnapshot> handleSupplyChainSnapshotResponse(Optional<SupplyChainSnapshot> snapshotOptional) {
        Platform.runLater(() -> {
            if (snapshotOptional.isEmpty()) {
                return;
            }
            snapshot = snapshotOptional.get();
            factoriesCountLabel.setText(String.valueOf(snapshot.getFactoryCount()));
            fallbackManager.setLoading(false);
            System.out.println("Snapshot received: " + snapshot);
        });

        return snapshotOptional;
    }
}
