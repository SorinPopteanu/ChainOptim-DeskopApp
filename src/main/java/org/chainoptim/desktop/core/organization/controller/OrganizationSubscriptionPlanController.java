package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.context.SupplyChainSnapshotContext;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.organization.model.OrganizationViewData;
import org.chainoptim.desktop.core.subscriptionplan.model.PlanDetails;
import org.chainoptim.desktop.core.subscriptionplan.model.SubscriptionPlans;
import org.chainoptim.desktop.shared.util.DataReceiver;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.Objects;

/*
 * Controller for the organization subscription plan view.
 * Displays the current organization feature counts vs plan limits
 * and allows user to preview and change plans.
 */
public class OrganizationSubscriptionPlanController implements DataReceiver<OrganizationViewData> {

    // State
    private OrganizationViewData organizationViewData;
    private final SupplyChainSnapshotContext snapshotContext;
    private PlanDetails planDetails;
    private Organization.SubscriptionPlanTier currentPlan;
    private Organization.SubscriptionPlanTier currentPreviewedPlan;

    // FXML
    @FXML
    private Label tabTitle;
    @FXML
    private Button changePlanButton;
    @FXML
    private Label priceLabel;
    @FXML
    private GridPane planGridPane;

    // Icons
    private Image editImage;

    @Inject
    public OrganizationSubscriptionPlanController(SupplyChainSnapshotContext snapshotContext) {
        this.snapshotContext = snapshotContext;
    }

    @Override
    public void setData(OrganizationViewData data) {
        this.organizationViewData = data;
        this.planDetails = organizationViewData.getOrganization().getSubscriptionPlan();
        this.currentPlan = organizationViewData.getOrganization().getSubscriptionPlanTier();
        this.currentPreviewedPlan = currentPlan;

        initializeIcons();
        initializeUI();
    }

    private void initializeUI() {
        tabTitle.setText("Subscription Plan: " + organizationViewData.getOrganization().getSubscriptionPlanTier().toString());

        styleEditButton(changePlanButton);
        changePlanButton.setText(currentPlan == currentPreviewedPlan ? "Change Plan" : "Continue");
        ContextMenu planPopover = createPlanPopover();
        // Show Preview Plan popover
        changePlanButton.setOnAction(event ->
            planPopover.show(changePlanButton, Side.BOTTOM, 0, 0));

        priceLabel.setText("$" + planDetails.getPricePerMonthDollars() + " / month");

        renderGridPane();
    }

    private void initializeIcons() {
        editImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pen-to-square-solid.png")));
    }

    private void renderGridPane() {
        planGridPane.getChildren().clear();
        planGridPane.getColumnConstraints().clear();

        addSectionRow("Organization", 0);
        addIntLimitRow("Members", organizationViewData.getOrganization().getUsers().size(), planDetails.getMaxMembers(), 1);
        addIntLimitRow("Custom Roles", 4, planDetails.getMaxRoles(), 2);

        addSectionRow("Notifications", 3);
        addBooleanRow("Real-time Notifications", planDetails.isRealTimeNotificationsOn(), 4);
        addBooleanRow("Email Notifications", planDetails.isEmailNotificationsOn(), 5);
        addBooleanRow("Custom Notifications", planDetails.isCustomNotificationsOn(), 6);

        addSectionRow("Products", 7);
        addIntLimitRow("Products", (int) snapshotContext.getSnapshot().getProductsCount(), planDetails.getMaxProducts(), 8);
        addIntLimitRow("Components", 0, planDetails.getMaxComponents(), 9);
        addIntLimitRow("Stages per Product", 0, planDetails.getMaxStagesPerProduct(), 10);

        addSectionRow("Factories", 11);
        addIntLimitRow("Factories", (int) snapshotContext.getSnapshot().getFactoriesCount(), planDetails.getMaxFactories(), 12);
        addIntLimitRow("Stages per Factory", 0, planDetails.getMaxStagesPerFactory(), 13);
        addIntLimitRow("Inventory Items per Factory", 0, planDetails.getMaxInventoryItemsPerFactory(), 14);
        addBooleanRow("Factory Performance", planDetails.isFactoryPerformanceOn(), 15);

        addSectionRow("Warehouses", 16);
        addIntLimitRow("Warehouses", (int) snapshotContext.getSnapshot().getWarehousesCount(), planDetails.getMaxWarehouses(), 17);
        addIntLimitRow("Inventory Items per Warehouse", 0, planDetails.getMaxInventoryItemsPerWarehouse(), 18);

        addSectionRow("Suppliers", 19);
        addIntLimitRow("Suppliers", (int) snapshotContext.getSnapshot().getSuppliersCount(), planDetails.getMaxSuppliers(), 20);
        addIntLimitRow("Orders per Supplier", 0, planDetails.getMaxOrdersPerSupplier(), 21);
        addIntLimitRow("Shipments per Supplier Order", 0, planDetails.getMaxShipmentsPerSupplierOrder(), 22);
        addBooleanRow("Supplier Performance", planDetails.isSupplierPerformanceOn(), 23);

        addSectionRow("Clients", 24);
        addIntLimitRow("Clients", (int) snapshotContext.getSnapshot().getClientsCount(), planDetails.getMaxClients(), 25);
        addIntLimitRow("Orders per Client", 0, planDetails.getMaxOrdersPerClient(), 26);
        addIntLimitRow("Shipments per Client Order", 0, planDetails.getMaxShipmentsPerClientOrder(), 27);
        addBooleanRow("Client Performance", planDetails.isClientPerformanceOn(), 28);
    }

    private void addSectionRow(String label, int rowIndex) {
        Label sectionLabel = new Label(label);
        sectionLabel.getStyleClass().setAll("parent-row");
        planGridPane.add(sectionLabel, 0, rowIndex);
        applyStandardMargin(sectionLabel);
    }

    private void addIntLimitRow(String feature, int value, int limit, int rowIndex) {
        Label label = new Label("• " + feature);
        label.getStyleClass().setAll("child-row");
        planGridPane.add(label, 0, rowIndex);
        applyStandardMargin(label);

        Label valueLabel = new Label();
        String maxMembers = limit == -1 ? "Unlimited" : String.valueOf(limit);
        valueLabel.setText(value + " / " + maxMembers);
        if (limit != -1 && value >= limit) {
            valueLabel.getStyleClass().setAll("limit-reached-label");
        } else {
            valueLabel.getStyleClass().setAll("child-row");
        }
        GridPane.setHalignment(valueLabel, HPos.CENTER);
        planGridPane.add(valueLabel, 2, rowIndex);
    }

    private void addBooleanRow(String feature, boolean value, int rowIndex) {
        Label label = new Label("• " + feature);
        label.getStyleClass().setAll("child-row");
        planGridPane.add(label, 0, rowIndex);
        applyStandardMargin(label);

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(value);
        checkBox.setMouseTransparent(true);
        checkBox.setFocusTraversable(false);
        GridPane.setHalignment(checkBox, HPos.CENTER);
        planGridPane.add(checkBox, 2, rowIndex);
    }

    private ContextMenu createPlanPopover() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-pref-width: 125px;");

        // Add a non-selectable label item for "Preview"
        Label previewLabel = new Label("Preview");
        previewLabel.getStyleClass().add("general-label");
        CustomMenuItem previewItem = new CustomMenuItem(previewLabel);
        previewItem.setHideOnClick(false);
        contextMenu.getItems().add(previewItem);

        // Separator
        contextMenu.getItems().add(new SeparatorMenuItem());

        // List of plans
        for (Organization.SubscriptionPlanTier plan : Organization.SubscriptionPlanTier.values()) {
            MenuItem planItem = new MenuItem(plan.toString());
            if (plan == currentPlan) {
                planItem.getStyleClass().add("general-label");
            }
            planItem.setOnAction(event -> {
                // Rerender with preview plan
                if (currentPreviewedPlan == plan) return;
                currentPreviewedPlan = plan;
                planDetails = SubscriptionPlans.getPlans().get(plan);
                initializeUI();
            });
            contextMenu.getItems().add(planItem);
        }

        return contextMenu;
    }

    // Utils
    private void styleEditButton(Button button) {
        button.getStyleClass().add("standard-write-button");
        button.setMinHeight(32);
        button.setGraphic(createImageView(editImage));
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }

    private void applyStandardMargin(Node node) {
        GridPane.setMargin(node, new Insets(10, 240, 10, 36));
    }
}
