package org.chainoptim.desktop.core.overview.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.notification.model.Notification;
import org.chainoptim.desktop.core.notification.service.MyWebSocketClient;
import org.chainoptim.desktop.core.notification.service.NotificationPersistenceService;
import org.chainoptim.desktop.core.notification.service.StompClient;
import org.chainoptim.desktop.core.notification.service.WebSocketService;
import org.chainoptim.desktop.core.overview.model.SupplyChainSnapshot;
import org.chainoptim.desktop.core.overview.service.SupplyChainSnapshotService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.core.user.service.UserService;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.user.util.TokenManager;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class OverviewController implements Initializable {

    // Services
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final SupplyChainSnapshotService supplyChainSnapshotService;
//    private final WebSocketService webSocketService;
    private final NotificationPersistenceService notificationPersistenceService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    // State
    private SupplyChainSnapshot snapshot;

    // FXML
    @FXML
    private VBox headerContainer;
    @FXML
    private Label titleLabel;
    @FXML
    private StackPane contentStackPane;
    @FXML
    private VBox contentContainer;
    @FXML
    private HBox entityCountsHBox;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private Label notificationsLabel;
    @FXML
    private VBox notificationsVBox;

    // Icons
    private Image alertImage;

    @Inject
    public OverviewController(AuthenticationService authenticationService,
                              UserService userService,
                              SupplyChainSnapshotService supplyChainSnapshotService,
//                              WebSocketService webSocketService,
                              NotificationPersistenceService notificationPersistenceService,
                              FXMLLoaderService fxmlLoaderService,
                              ControllerFactory controllerFactory,
                              FallbackManager fallbackManager) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.supplyChainSnapshotService = supplyChainSnapshotService;
//        this.webSocketService = webSocketService;
        this.notificationPersistenceService = notificationPersistenceService;
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
        initializeUI();

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        authenticationService.getUsernameFromJWTToken(jwtToken).ifPresent(this::fetchAndSetUser);
    }

    // Initialization
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
            contentStackPane.setVisible(newValue);
            contentStackPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void initializeUI() {
        Image headerImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/globe-solid-black.png")));
        ImageView headerImageView = new ImageView(headerImage);
        headerImageView.setFitHeight(16);
        headerImageView.setFitWidth(16);
        titleLabel.setGraphic(headerImageView);

//        alertImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/alert-circle-solid.png")));
    }

    // Fetches
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
            String userIdParam = "?userId=" + user.getId(); // Or use jwtToken
            System.out.println("Connecting to WebSocket with userId: " + user.getId());

            try {
                URI serverUri = new URI("ws://localhost:8080/ws" + userIdParam);
                MyWebSocketClient client = new MyWebSocketClient(serverUri);
                client.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            // Set user to TenantContext for reuse throughout the app
            TenantContext.setCurrentUser(user);

            Integer organizationId = user.getOrganization().getId();
            if (organizationId == null) return;

            // Fetch Supply Chain snapshot and Notifications on separate threads
            supplyChainSnapshotService.getSupplyChainSnapshot(organizationId)
                    .thenApply(this::handleSupplyChainSnapshotResponse)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supply chain snapshot."));
                        return Optional.empty();
                    });

            notificationPersistenceService.getNotificationsByUserId(user.getId())
                    .thenApply(this::handleNotificationsResponse)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load notifications."));
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
            System.out.println("Snapshot received: " + snapshot);
            fallbackManager.setLoading(false);

            renderEntityCountsVBox(snapshot);
        });

        return snapshotOptional;
    }

    private Optional<List<Notification>> handleNotificationsResponse(Optional<List<Notification>> notificationsOptional) {
        Platform.runLater(() -> {
            if (notificationsOptional.isEmpty()) {
                return;
            }
            List<Notification> notifications = notificationsOptional.get();
            System.out.println("Notifications received: " + notifications.size());
            renderNotificationsVBox(notifications);
        });

        return notificationsOptional;
    }

    // UI Rendering
    private void renderEntityCountsVBox(SupplyChainSnapshot snapshot) {
        entityCountsHBox.getChildren().clear();

        HBox productsCountLabel = getFeatureCountBadge("Products", snapshot.getProductsCount());
        HBox factoriesCountLabel = getFeatureCountBadge("Factories", snapshot.getFactoriesCount());
        HBox warehousesCountLabel = getFeatureCountBadge("Warehouses", snapshot.getWarehousesCount());
        HBox suppliersCountLabel = getFeatureCountBadge("Suppliers", snapshot.getSuppliersCount());
        HBox clientsCountLabel = getFeatureCountBadge("Clients", snapshot.getClientsCount());


        entityCountsHBox.getChildren().addAll(productsCountLabel, factoriesCountLabel, warehousesCountLabel, suppliersCountLabel, clientsCountLabel);
        entityCountsHBox.setSpacing(40);
        entityCountsHBox.setAlignment(Pos.CENTER);
    }

    private HBox getFeatureCountBadge(String featureName, long count) {
        HBox badgeContainer = new HBox(0); // Adjust spacing as needed
        badgeContainer.setAlignment(Pos.CENTER_LEFT);
        badgeContainer.getStyleClass().add("badge-container");

        Label featureLabel = new Label(featureName);
        featureLabel.getStyleClass().add("feature-count-label");

        // Creating a region to act as a separator
        Region separator = new Region();
        separator.setPrefWidth(1);
        separator.setMinHeight(20);
        separator.setStyle("-fx-background-color: #d3d6d4;");

        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("count-label");

        badgeContainer.getChildren().addAll(featureLabel, separator, countLabel);

        return badgeContainer;
    }

    private void renderNotificationsVBox(List<Notification> notifications) {
        notificationsLabel.setText("Notifications (" + notifications.size() + ")");

        notificationsVBox.getChildren().clear();

        notifications.forEach(notification -> {
            HBox notificationHBox = new HBox(0);
            notificationHBox.setAlignment(Pos.CENTER_LEFT);
            notificationHBox.getStyleClass().add("notification-container");

            Label notificationLabel = new Label(notification.getMessage());
            notificationLabel.getStyleClass().add("notification-message");
            notificationHBox.getChildren().add(notificationLabel);

            Region separator = new Region();
            // Grow
            HBox.setHgrow(separator, Priority.ALWAYS);
            notificationHBox.getChildren().add(separator);

            // Alert indicator


            // Read indicator
            Circle indicator = new Circle(4);
            indicator.setFill(Color.TRANSPARENT);

            if (Boolean.FALSE.equals(notification.getReadStatus())) {
                indicator.setFill(Color.web("#006AEE"));
            }
            notificationHBox.getChildren().add(indicator);

            notificationsVBox.getChildren().add(notificationHBox);
        });
    }

}
