package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.util.DataReceiver;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class OrganizationOverviewController implements DataReceiver<Organization> {

    private Organization organization;

    @FXML
    private VBox usersVBox;

    @Override
    public void setData(Organization data) {
        this.organization = data;

        initializeUI();

    }

    private void initializeUI() {
        if (organization.getUsers() == null) {
            return;
        }

        for (User user : organization.getUsers()) {
            HBox rowHbox = new HBox();
            rowHbox.setSpacing(120);
            Label usernameLabel = new Label(user.getUsername());
            rowHbox.getChildren().add(usernameLabel);
            Label roleLabel = new Label(user.getRole().toString());
            rowHbox.getChildren().add(roleLabel);
            if (user.getCustomRole() != null) {
                Label customRoleLabel = new Label(user.getCustomRole().getName());
                rowHbox.getChildren().add(customRoleLabel);
            } else {
                rowHbox.getChildren().add(new Label("Not assigned"));
            }
            usersVBox.getChildren().add(rowHbox);
        }
    }
}
