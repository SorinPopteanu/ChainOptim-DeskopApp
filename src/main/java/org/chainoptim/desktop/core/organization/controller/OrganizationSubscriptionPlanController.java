package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.SupplyChainSnapshotContext;
import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.organization.model.OrganizationViewData;
import org.chainoptim.desktop.core.subscriptionplan.model.PlanDetails;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogActionListener;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import static javafx.scene.control.PopupControl.USE_PREF_SIZE;

/*
 * Controller for the organization subscription plan view.
 * Currently only exemplifies the Generic Confirm Dialog implementation pattern.
 */
public class OrganizationSubscriptionPlanController implements DataReceiver<OrganizationViewData> {

    // State
    private OrganizationViewData organizationViewData;
    private SupplyChainSnapshotContext snapshotContext;
    private PlanDetails planDetails;

    // FXML
    @FXML
    private Label tabTitle;
    @FXML
    private Button changePlanButton;
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

        initializeUI();
    }

    private void initializeUI() {
        initializeIcons();

        tabTitle.setText("Subscription Plan: " + organizationViewData.getOrganization().getSubscriptionPlanTier().toString());
        styleEditButton(changePlanButton);

        renderGridPane();
    }

    private void initializeIcons() {
        editImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pen-to-square-solid.png")));
    }

    private void renderGridPane() {
        planGridPane.getChildren().clear();
        planGridPane.getColumnConstraints().clear();
        GridPane.setHgrow(planGridPane, Priority.ALWAYS);
        planGridPane.setMinWidth(USE_PREF_SIZE);

        // Define constraints for all three columns
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        col1.setPercentWidth(30);
        col2.setHgrow(Priority.ALWAYS);
        col3.setPercentWidth(30);
        planGridPane.getColumnConstraints().addAll(col1, col2, col3);

        Label organizationLabel = new Label("Organization:");
        organizationLabel.getStyleClass().setAll("parent-row");
        planGridPane.add(organizationLabel, 0, 0);

        Label maxMembersLabel = new Label("Maximum Members:");
        maxMembersLabel.getStyleClass().setAll("child-row");
        planGridPane.add(maxMembersLabel, 0, 1);

        Label maxMembersValue = new Label();
        String maxMembers = planDetails.getMaxMembers() == -1 ? "Unlimited" : String.valueOf(planDetails.getMaxMembers());
        String currentMembers = String.valueOf(organizationViewData.getOrganization().getUsers().size());
        maxMembersValue.setText(currentMembers + " / " + maxMembers);
        maxMembersValue.getStyleClass().setAll("child-row");
        planGridPane.add(maxMembersValue, 2, 1);
    }

    private void styleEditButton(Button button) {
        button.setText("Change Plan");
        button.getStyleClass().add("standard-write-button");
        button.setGraphic(createImageView(editImage));
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }
}
