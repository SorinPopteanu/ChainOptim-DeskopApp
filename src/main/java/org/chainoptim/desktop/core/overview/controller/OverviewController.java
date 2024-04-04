package org.chainoptim.desktop.core.overview.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.SupplyChainSnapshotContext;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.notification.model.NotificationUser;
import org.chainoptim.desktop.core.notification.service.NotificationPersistenceService;
import org.chainoptim.desktop.core.overview.model.Snapshot;
import org.chainoptim.desktop.core.overview.model.SupplyChainSnapshot;
import org.chainoptim.desktop.core.overview.service.SupplyChainSnapshotService;
import org.chainoptim.desktop.core.user.model.User;
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

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/*
 * Controller for the initial overview screen of the application.
 * Loads and displays a snapshot of the organization's supply chain
 * and the current user's notifications history.
 */
public class OverviewController implements Initializable {

    // Services
    private final SupplyChainSnapshotService supplyChainSnapshotService;
    private final NotificationPersistenceService notificationPersistenceService;
    private final NavigationService navigationService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    // State
    private Snapshot snapshot;
    private SupplyChainSnapshotContext snapshotContext;

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
    public OverviewController(SupplyChainSnapshotService supplyChainSnapshotService,
                              NotificationPersistenceService notificationPersistenceService,
                              NavigationService navigationService,
                              FXMLLoaderService fxmlLoaderService,
                              ControllerFactory controllerFactory,
                              FallbackManager fallbackManager,
                              SupplyChainSnapshotContext snapshotContext) {
        this.supplyChainSnapshotService = supplyChainSnapshotService;
        this.notificationPersistenceService = notificationPersistenceService;
        this.navigationService = navigationService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
        this.snapshotContext = snapshotContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
        setUpListeners();
        initializeUI();
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

        // Listen to current user changes
        TenantContext.currentUserProperty().addListener((observable, oldValue, newValue) -> loadData(newValue));
    }

    private void initializeUI() {
        Image headerImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/globe-solid-black.png")));
        ImageView headerImageView = new ImageView(headerImage);
        headerImageView.setFitHeight(16);
        headerImageView.setFitWidth(16);
        titleLabel.setGraphic(headerImageView);

//        alertImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/alert-circle-solid.png")));
    }

    private void loadData(User currentUser) {
        if (currentUser == null) return;
        Integer organizationId = currentUser.getOrganization().getId();
        if (organizationId == null) return;

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        // Fetch Supply Chain snapshot and Notifications on separate threads
        supplyChainSnapshotService.getSupplyChainSnapshot(organizationId)
                .thenApply(this::handleSupplyChainSnapshotResponse)
                .exceptionally(ex -> {
                    Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supply chain snapshot."));
                    return Optional.empty();
                });

        notificationPersistenceService.getNotificationsByUserId(currentUser.getId())
                .thenApply(this::handleNotificationsResponse)
                .exceptionally(ex -> {
                    Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load notifications."));
                    return Optional.empty();
                });
    }

    // Fetches
    private Optional<SupplyChainSnapshot> handleSupplyChainSnapshotResponse(Optional<SupplyChainSnapshot> snapshotOptional) {
        Platform.runLater(() -> {
            if (snapshotOptional.isEmpty()) {
                return;
            }
            snapshot = snapshotOptional.get().getSnapshot();
            snapshotContext.setSnapshot(snapshot);
            fallbackManager.setLoading(false);

            renderEntityCountsVBox(snapshot);
        });

        return snapshotOptional;
    }

    private Optional<List<NotificationUser>> handleNotificationsResponse(Optional<List<NotificationUser>> notificationsOptional) {
        Platform.runLater(() -> {
            if (notificationsOptional.isEmpty()) {
                return;
            }
            List<NotificationUser> notifications = notificationsOptional.get();
            fallbackManager.setLoading(false);

            renderNotificationsVBox(notifications);
        });

        return notificationsOptional;
    }

    // UI Rendering
    private void renderEntityCountsVBox(Snapshot snapshot) {
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
        HBox badgeContainer = new HBox(0);
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
        badgeContainer.setOnMouseClicked(event -> navigationService.switchView(featureName, true));

        return badgeContainer;
    }

    private void renderNotificationsVBox(List<NotificationUser> notifications) {
        notificationsLabel.setText("Notifications (" + notifications.size() + ")");

        notificationsVBox.getChildren().clear();

        notifications.forEach(notification -> {
            HBox notificationHBox = new HBox(0);
            notificationHBox.setAlignment(Pos.CENTER_LEFT);
            notificationHBox.getStyleClass().add("notification-container");

            Label notificationLabel = new Label(notification.getNotification().getMessage());
            notificationLabel.getStyleClass().add("notification-message");
            notificationHBox.getChildren().add(notificationLabel);

            Region separator = new Region();
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
