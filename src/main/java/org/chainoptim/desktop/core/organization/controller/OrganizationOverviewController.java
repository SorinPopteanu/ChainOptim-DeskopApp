package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.util.DataReceiver;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.Objects;

public class OrganizationOverviewController implements DataReceiver<Organization> {

    // State
    private Organization organization;
    private boolean isDeleteMode = false;

    // FXML
    @FXML
    private Label tabTitle;
    @FXML
    private Button removeMemberButton;
    @FXML
    private Button addNewMemberButton;
    @FXML
    private GridPane membersGridPane;

    // Constants
    String[] headers = {"Username", "Role", "Custom Role", "Joined At", "Email"};

    // Icons
    private Image plusImage;
    private Image trashImage;

    @Override
    public void setData(Organization data) {
        this.organization = data;

        initializeUI();
    }

    private void initializeUI() {
        if (organization.getUsers() == null) {
            return;
        }

        initializeIcons();

        // Initialize title container
        tabTitle.setText("Members (" + organization.getUsers().size() + ")");
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
        for (User user : organization.getUsers()) {
            for (int i = 0; i < headers.length; i++) {
                String property = getDisplayedPropertyByHeader(user, headers[i]);
                Label label = new Label(property);
                applyStandardMargin(label);
                membersGridPane.add(label, i, row);
                GridPane.setHalignment(label, HPos.CENTER);
            }
            row++;
        }
    }

    private String getDisplayedPropertyByHeader(User user, String header) {
        return switch (header) {
            case "Username" -> user.getUsername();
            case "Role" -> user.getRole().toString();
            case "Custom Role" -> user.getCustomRole() != null ? user.getCustomRole().getName() : "Not assigned";
            case "Joined At" -> user.getCreatedAt().toString();
            case "Email" -> user.getEmail();
            default -> null;
        };
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
