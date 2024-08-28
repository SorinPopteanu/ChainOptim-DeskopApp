package org.chainoptim.desktop.core.tenant.customrole.controller;

import org.chainoptim.desktop.core.tenant.user.model.User;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.controller.GenericConfirmDialogActionListener;
import org.chainoptim.desktop.shared.util.DataReceiver;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import lombok.Setter;

import java.util.Objects;

public class OrganizationAssignBasicRoleController implements DataReceiver<User> {

    // State
    private User user;
    private User.Role originalSelectedRole;
    private User.Role selectedRole;
    private boolean hasRenderedWarning;

    // Listeners
    @Setter
    private GenericConfirmDialogActionListener<Pair<String, User.Role>> actionListener; // userId, basic role

    // FXML
    @FXML
    private Label titleLabel;
    @FXML
    private VBox rolesVBox;
    @FXML
    private TextFlow messageTextFlowContainer;
    @FXML
    private VBox permissionsVBox;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    @Override
    public void setData(User data) {
        user = data;
        originalSelectedRole = user.getRole();
        selectedRole = originalSelectedRole;

        initializeUI();
    }

    private void initializeUI() {
        titleLabel.setText("Assign role to " + user.getUsername());

        for (User.Role role : User.Role.values()) {
            String label = role.toString();
            label = label.substring(0, 1).toUpperCase() + label.substring(1).toLowerCase();
            Button selectRoleButton = new Button(label);
            boolean isSelected = role.equals(selectedRole);
            styleSelectButton(selectRoleButton, isSelected);
            selectRoleButton.setOnAction(this::selectRole);

            rolesVBox.getChildren().add(selectRoleButton);
        }
    }

    private void selectRole(ActionEvent event) {
        Button source = (Button) event.getSource();

        // Select role, accounting for None button
        int roleIndex = rolesVBox.getChildren().indexOf(source);
        selectedRole = User.Role.values()[roleIndex];
        styleSelectButton(source, true);

        // Deselect other roles
        for (int i = 0; i < rolesVBox.getChildren().size(); i++) {
            if (i != roleIndex) {
                Node otherButton = rolesVBox.getChildren().get(i);
                if (otherButton instanceof Button button) {
                    styleSelectButton(button, false);
                }
            }
        }

        // Render permissions to be granted
        renderPermissionsVBox();

        // On first selection, render warning message
        if (hasRenderedWarning) return;
        String message = "Are you sure you want to assign this role to " + user.getUsername() + "? ";
        if (user.getCustomRole() != null) {
            message += "This will have no effect as the user already has the custom role " + user.getCustomRole().getName() + ".";
        } else {
            message += "This will grant them the following permissions:";
        }
        messageTextFlowContainer.getChildren().clear();
        messageTextFlowContainer.getChildren().add(new Text(message));
        hasRenderedWarning = true;
    }

    private void renderPermissionsVBox() {
        if (selectedRole == null) return;

        permissionsVBox.getChildren().clear();

        Label permissionsLabel = switch (selectedRole) {
            case ADMIN -> new Label("To Read, Create, Update, Delete all features");
            case MEMBER -> new Label("To Read all features");
            case NONE -> new Label("No permissions granted");
        };

        permissionsLabel.getStyleClass().add("general-label");
        permissionsVBox.getChildren().add(permissionsLabel);
    }

    @FXML
    private void onConfirmButtonClicked() {
        if (selectedRole != null && Objects.equals(selectedRole, originalSelectedRole)) return; // Skip if already assigned

        Pair<String, User.Role> data = new Pair<>(user.getId(), selectedRole);
        actionListener.onConfirmAction(data);
    }

    @FXML
    private void onCancelButtonClicked() {
        actionListener.onCancelAction();
    }

    private void styleSelectButton(Button selectButton, boolean isSelected) {
        if (isSelected) {
            styleSelectedButton(selectButton);
        } else {
            styleUnselectedButton(selectButton);
        }
    }

    private void styleUnselectedButton(Button selectButton) {
        selectButton.setMaxWidth(Double.MAX_VALUE);
        selectButton.getStyleClass().clear();
        selectButton.getStyleClass().add("assign-role-element");
    }

    private void styleSelectedButton(Button selectButton) {
        selectButton.setMaxWidth(Double.MAX_VALUE);
        selectButton.getStyleClass().clear();
        selectButton.getStyleClass().add("assign-role-selected-element");
    }
}
