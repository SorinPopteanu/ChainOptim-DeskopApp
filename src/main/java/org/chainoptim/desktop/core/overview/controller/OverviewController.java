package org.chainoptim.desktop.core.overview.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.SupplyChainSnapshotContext;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.map.MapController;
import org.chainoptim.desktop.core.map.model.SupplyChainMap;
import org.chainoptim.desktop.core.map.service.SupplyChainMapService;
import org.chainoptim.desktop.core.notification.model.NotificationExtraInfo;
import org.chainoptim.desktop.core.notification.model.NotificationUser;
import org.chainoptim.desktop.core.notification.service.NotificationPersistenceService;
import org.chainoptim.desktop.core.overview.model.Snapshot;
import org.chainoptim.desktop.core.overview.model.SupplyChainSnapshot;
import org.chainoptim.desktop.core.overview.service.SupplyChainSnapshotService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.common.uielements.badge.BadgeData;
import org.chainoptim.desktop.shared.common.uielements.badge.FeatureCountBadge;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
import java.util.*;

/**
 * Controller for the initial overview screen of the application.
 * Loads and displays a snapshot of the organization's supply chain
 * and the current user's notifications history.
 */
public class OverviewController implements Initializable {

    // Services
    private final SupplyChainSnapshotService supplyChainSnapshotService;
    private final NotificationPersistenceService notificationPersistenceService;
    private final NavigationService navigationService;
    private final CommonViewsLoader commonViewsLoader;

    // State
    private final SearchParams searchParams;
    private final FallbackManager fallbackManager;
    private Snapshot snapshot;
    private final SupplyChainSnapshotContext snapshotContext;
    private long totalCount;
    private int lastPage = 1;
    private boolean clearNotifications = true;
    private Button currentLoadMoreButton;

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
    @FXML
    private Button renderMapButton;
    @FXML
    private StackPane mapContainer;

    @Inject
    public OverviewController(SupplyChainSnapshotService supplyChainSnapshotService,
                              NotificationPersistenceService notificationPersistenceService,
                              NavigationService navigationService,
                              CommonViewsLoader commonViewsLoader,
                              SearchParams searchParams,
                              FallbackManager fallbackManager,
                              SupplyChainSnapshotContext snapshotContext) {
        this.supplyChainSnapshotService = supplyChainSnapshotService;
        this.notificationPersistenceService = notificationPersistenceService;
        this.navigationService = navigationService;
        this.commonViewsLoader = commonViewsLoader;
        this.searchParams = searchParams;
        this.fallbackManager = fallbackManager;
        this.snapshotContext = snapshotContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setUpListeners();
        initializeUI();
    }

    private void setUpListeners() {
        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            contentContainer.setVisible(newValue);
            contentContainer.setManaged(newValue);
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
                    return new Result<>();
                });

        notificationPersistenceService.getNotificationsByUserIdAdvanced(currentUser.getId(), searchParams)
                .thenApply(this::handleNotificationsResponse)
                .exceptionally(ex -> {
                    Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load notifications."));
                    return new Result<>();
                });
    }

    // Fetches
    private Result<SupplyChainSnapshot> handleSupplyChainSnapshotResponse(Result<SupplyChainSnapshot> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                return;
            }
            snapshot = result.getData().getSnapshot();
            snapshotContext.setSnapshot(snapshot);
            fallbackManager.setLoading(false);

            renderEntityCountsVBox(snapshot);
        });

        return result;
    }

    private Result<PaginatedResults<NotificationUser>> handleNotificationsResponse(Result<PaginatedResults<NotificationUser>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                return;
            }
            PaginatedResults<NotificationUser> notifications = result.getData();
            fallbackManager.setLoading(false);

            totalCount = notifications.getTotalCount();
            renderNotificationsVBox(notifications.getResults());
        });

        return result;
    }

    // UI Rendering
    private void renderEntityCountsVBox(Snapshot snapshot) {
        entityCountsHBox.getChildren().clear();

        FeatureCountBadge productsCountLabel = new FeatureCountBadge(new BadgeData("Products", snapshot.getProductsCount(), () -> navigationService.switchView("Products", true, null)));
        FeatureCountBadge factoriesCountLabel = new FeatureCountBadge(new BadgeData("Factories", snapshot.getFactoriesCount(), () -> navigationService.switchView("Factories", true, null)));
        FeatureCountBadge warehousesCountLabel = new FeatureCountBadge(new BadgeData("Warehouses", snapshot.getWarehousesCount(), () -> navigationService.switchView("Warehouses", true, null)));
        FeatureCountBadge suppliersCountLabel = new FeatureCountBadge(new BadgeData("Suppliers", snapshot.getSuppliersCount(), () -> navigationService.switchView("Suppliers", true, null)));
        FeatureCountBadge clientsCountLabel = new FeatureCountBadge(new BadgeData("Clients", snapshot.getClientsCount(), () -> navigationService.switchView("Clients", true, null)));

        entityCountsHBox.getChildren().addAll(productsCountLabel, factoriesCountLabel, warehousesCountLabel, suppliersCountLabel, clientsCountLabel);
        entityCountsHBox.setSpacing(40);
        entityCountsHBox.setAlignment(Pos.CENTER);
    }

    private void renderNotificationsVBox(List<NotificationUser> notifications) {
        notificationsLabel.setText("Notifications (" + totalCount + ")");

        if (clearNotifications) {
            notificationsVBox.getChildren().clear();
        }

        notifications.forEach(this::renderNotification);

        addLoadMoreButton();
    }

    private void renderNotification(NotificationUser notification) {
        HBox notificationHBox = new HBox(0);
        notificationHBox.setAlignment(Pos.CENTER_LEFT);
        notificationHBox.getStyleClass().add("notification-container");

        VBox titleMessageVBox = new VBox(6);
        Label notificationTitleLabel = new Label(notification.getNotification().getTitle());
        notificationTitleLabel.getStyleClass().add("notification-title");
        titleMessageVBox.getChildren().add(notificationTitleLabel);

        Label notificationMessageLabel = new Label(notification.getNotification().getMessage());
        notificationMessageLabel.getStyleClass().add("notification-message");
        titleMessageVBox.getChildren().add(notificationMessageLabel);

        notificationHBox.getChildren().add(titleMessageVBox);

        Region separator = new Region();
        HBox.setHgrow(separator, Priority.ALWAYS);
        notificationHBox.getChildren().add(separator);

        // Alert indicator


        // Extra details
        NotificationExtraInfo extraInfo = notification.getNotification().getExtraInfo();
        if (extraInfo != null && !extraInfo.getExtraMessages().isEmpty()) {
            Button extraInfoButton = new Button("View Details");
            extraInfoButton.getStyleClass().add("pseudo-link");
            extraInfoButton.setStyle("-fx-padding: 0px 8px;");
            notificationHBox.getChildren().add(extraInfoButton);
        }

        // Read indicator
        Circle indicator = new Circle(4);
        indicator.setFill(Color.TRANSPARENT);

        if (Boolean.FALSE.equals(notification.getReadStatus())) {
            indicator.setFill(Color.web("#006AEE"));
        }
        notificationHBox.getChildren().add(indicator);

        notificationsVBox.getChildren().add(notificationHBox);
    }

    private void addLoadMoreButton() {
        notificationsVBox.getChildren().remove(currentLoadMoreButton);
        if (totalCount <= notificationsVBox.getChildren().size()) return;

        currentLoadMoreButton = new Button("Load More");
        currentLoadMoreButton.getStyleClass().add("pseudo-link");
        currentLoadMoreButton.setStyle("-fx-padding: 10px 12px;");
        currentLoadMoreButton.setOnAction(event -> {
            searchParams.setPage(++lastPage);
            clearNotifications = false;
            notificationPersistenceService.getNotificationsByUserIdAdvanced(TenantContext.getCurrentUser().getId(), searchParams)
                    .thenApply(this::handleNotificationsResponse)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load more notifications."));
                        return new Result<>();
                    });
        });
        notificationsVBox.getChildren().add(currentLoadMoreButton);
    }
}
