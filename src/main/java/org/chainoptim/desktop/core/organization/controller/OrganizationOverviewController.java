package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.organization.model.OrganizationViewData;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class OrganizationOverviewController implements DataReceiver<OrganizationViewData> {

    // Services
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    // State
    private OrganizationViewData organizationViewData;
    private boolean isDeleteMode = false;
    private final FallbackManager fallbackManager;

    // FXML
    @FXML
    private Label tabTitle;
    @FXML
    private Button removeMemberButton;
    @FXML
    private Button addNewMemberButton;
    @FXML
    private GridPane membersGridPane;
//    @FXML
//    private StackPane assignRoleDialogContainer;

    // Constants
    String[] headers = {"Username", "Role", "Custom Role", "Joined At", "Email"};

    // Icons
    private Image plusImage;
    private Image trashImage;

    @Inject
    public OrganizationOverviewController(FXMLLoaderService fxmlLoaderService,
                                          ControllerFactory controllerFactory,
                                          FallbackManager fallbackManager) {
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(OrganizationViewData data) {
        this.organizationViewData = data;

        initializeOrganizationUI();
    }

    private void initializeOrganizationUI() {
        Set<User> users = organizationViewData.getOrganization().getUsers();
        if (users == null) {
            fallbackManager.setErrorMessage("Failed to load organization members");
            return;
        }

        initializeIcons();

        // Initialize title container
        tabTitle.setText("Members (" + users.size() + ")");
        styleDeleteRoleButton(removeMemberButton);
        removeMemberButton.setOnAction(event -> toggleDeleteMode());
        styleAddNewRoleButton(addNewMemberButton);
        addNewMemberButton.setOnAction(event -> {});

        // Render members grid
        renderMembersGrid();
    }

    private void initializeIcons() {
        plusImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/plus.png")));
        trashImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/trash-solid.png")));
    }

    private void renderMembersGrid() {
        membersGridPane.getChildren().clear();

        // Headers
        for (int i = 0; i < headers.length; i++) {
            Label label = new Label(headers[i]);
            label.getStyleClass().add("column-header");
            membersGridPane.add(label, i, 0);
            GridPane.setHalignment(label, HPos.CENTER);
        }

        // Rows
        int row = 1;
        for (User user : organizationViewData.getOrganization().getUsers()) {
            for (int i = 0; i < headers.length; i++) {
                Node node = getDisplayedPropertyByHeader(user, headers[i]);
                if (i > 0) applyStandardMargin(node);
                membersGridPane.add(node, i, row);
                GridPane.setHalignment(node, HPos.CENTER);
            }
            row++;
        }
    }

    private Node getDisplayedPropertyByHeader(User user, String header) {
        if (header.equals("Custom Role")) {
            if (user.getCustomRole() != null) {
                return new Label(user.getCustomRole().getName());
            }
            Button button = new Button("Assign");
            button.getStyleClass().add("pseudo-link");
            button.setOnAction(event -> loadAssignRoleDialog(user, event));
            return button;
        }
        return switch (header) {
            case "Username" -> new Label(user.getUsername());
            case "Role" -> new Label(user.getRole().toString());
            case "Joined At" -> new Label(user.getCreatedAt().toString());
            case "Email" -> new Label(user.getEmail());
            default -> null;
        };
    }

    private void loadAssignRoleDialog(User user, ActionEvent event) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/organization/OrganizationAssignRoleView.fxml",
                controllerFactory::createController
        );
        try {
            Node content = loader.load();
            OrganizationAssignRoleController controller = loader.getController();
            controller.setData(new Pair<>(user, organizationViewData.getCustomRoles()));

            Popup popup = new Popup();
            popup.getContent().add(content);
            popup.setAutoHide(true);

            Button sourceButton = (Button) event.getSource(); // Cast is safe here since we know the source is a Button
            double x = sourceButton.localToScreen(sourceButton.getBoundsInLocal()).getMinX();
            double y = sourceButton.localToScreen(sourceButton.getBoundsInLocal()).getMaxY();
            popup.show(sourceButton, x, y);
            // What here instead of this??
//            assignRoleDialogContainer.getChildren().add(content);
//            assignRoleDialogContainer.setVisible(true);
//            assignRoleDialogContainer.setManaged(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void toggleDeleteMode() {
        isDeleteMode = !isDeleteMode;
    }


    // TODO: Take this into separate class and reuse
    private void styleAddNewRoleButton(Button button) {
        button.getStyleClass().add("standard-write-button");
        button.setGraphic(createImageView(plusImage));
        button.setText("Add New Member");
        applyStandardMargin(button);
    }

    private void styleDeleteRoleButton(Button button) {
        button.getStyleClass().add("standard-delete-button");
        button.setGraphic(createImageView(trashImage));
        button.setTooltip(new Tooltip("Delete Member"));
        GridPane.setMargin(button, new Insets(12));
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }

    private void applyStandardMargin(Node node) {
        GridPane.setMargin(node, new Insets(12, 36, 12, 36));
    }
}
