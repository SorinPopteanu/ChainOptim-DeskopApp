package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.organization.model.FeaturePermissions;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogActionListener;
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

import java.util.List;
import java.util.Objects;

public class OrganizationAssignCustomRoleController implements DataReceiver<Pair<User, Pair<List<CustomRole>, CustomRole>>> {

    // State
    private User user;
    private List<CustomRole> customRoles;
    private CustomRole originalSelectedRole;
    private CustomRole selectedRole;
    private boolean hasRenderedWarning;

    // Listeners
    @Setter
    private GenericConfirmDialogActionListener<Pair<String, Integer>> actionListener; // userId, customRoleId

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
    public void setData(Pair<User, Pair<List<CustomRole>, CustomRole>> data) {
        user = data.getKey();
        customRoles = data.getValue().getKey();
        originalSelectedRole = data.getValue().getValue();
        selectedRole = originalSelectedRole;

        initializeUI();
    }

    private void initializeUI() {
        if (customRoles == null) return;

        titleLabel.setText("Assign role to " + user.getUsername());

        // Add None Option
        Button selectNoneButton = new Button("None");
        styleSelectButton(selectNoneButton, selectedRole == null);
        selectNoneButton.setOnAction(this::selectRole);
        rolesVBox.getChildren().add(selectNoneButton);

        // Add role buttons
        for (CustomRole customRole : customRoles) {
            Button selectRoleButton = new Button(customRole.getName());
            boolean isSelected = selectedRole != null && customRole.getId().equals(selectedRole.getId());
            styleSelectButton(selectRoleButton, isSelected);
            selectRoleButton.setOnAction(this::selectRole);


            rolesVBox.getChildren().add(selectRoleButton);
        }
    }

    private void selectRole(ActionEvent event) {
        Button source = (Button) event.getSource();

        // Select role, accounting for None button
        int roleIndex = rolesVBox.getChildren().indexOf(source);
        selectedRole = roleIndex > 0 ? customRoles.get(roleIndex - 1) : null;
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
        String message = "Are you sure you want to assign this role to " + user.getUsername() + "?"
                + " This will override the basic role " + user.getRole().toString()
                + " and grant them the following permissions:";
        messageTextFlowContainer.getChildren().clear();
        messageTextFlowContainer.getChildren().add(new Text(message));
        hasRenderedWarning = true;
    }

    private void renderPermissionsVBox() {
        if (selectedRole == null || selectedRole.getPermissions() == null) return;

        permissionsVBox.getChildren().clear();

        renderFeaturePermissions(selectedRole.getPermissions().getProducts(), "products");
        renderFeaturePermissions(selectedRole.getPermissions().getFactories(), "factories");
        renderFeaturePermissions(selectedRole.getPermissions().getWarehouses(), "warehouses");
        renderFeaturePermissions(selectedRole.getPermissions().getSuppliers(), "suppliers");
        renderFeaturePermissions(selectedRole.getPermissions().getClients(), "clients");
    }

    private void renderFeaturePermissions(FeaturePermissions featurePermissions, String featureName) {
        if (featurePermissions == null) return;

        String permissionText = "To ";
        if (Boolean.TRUE.equals(featurePermissions.getCanRead())) {
            permissionText += "Read, ";
        }
        if (Boolean.TRUE.equals(featurePermissions.getCanCreate())) {
            permissionText += "Create, ";
        }
        if (Boolean.TRUE.equals(featurePermissions.getCanUpdate())) {
            permissionText += "Update, ";
        }
        if (Boolean.TRUE.equals(featurePermissions.getCanDelete())) {
            permissionText += "Delete, ";
        }
        permissionText = permissionText.substring(0, permissionText.length() - 2); // Remove trailing comma and space

        if (permissionText.length() > 1) {
            permissionText += " " + featureName;

            Label featureLabel = new Label(permissionText);
            featureLabel.getStyleClass().add("general-label");
            permissionsVBox.getChildren().add(featureLabel);
        }
    }

    @FXML
    private void onConfirmButtonClicked() {
        Integer selectedRoleId = selectedRole == null ? null : selectedRole.getId(); // Allow null = no custom role
        if (selectedRole != null && originalSelectedRole != null && Objects.equals(selectedRole.getId(), originalSelectedRole.getId())) return; // Skip if already assigned

        Pair<String, Integer> data = new Pair<>(user.getId(), selectedRoleId);
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
