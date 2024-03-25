package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.organization.model.ConfirmDialogActionListener;
import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.core.user.service.UserService;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

public class ConfirmCustomRoleUpdateController implements DataReceiver<CustomRole> {

    private final UserService userService;

    private CustomRole customRole;

    @Setter
    private ConfirmDialogActionListener confirmDialogActionListener;

    @FXML
    private VBox usersVBox;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    @Inject
    public ConfirmCustomRoleUpdateController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setData(CustomRole data) {
        this.customRole = data;
        System.out.println("Custom role: " + customRole.getName());

        userService.getUsersByCustomRoleId(customRole.getId())
                .thenApply(this::handleUsersResponse)
                .exceptionally(this::handleUsersException);
    }

    private Optional<List<User>> handleUsersResponse(Optional<List<User>> users) {
        if (users.isEmpty()) {
            System.out.println("No users found for custom role: " + customRole.getName());
            return Optional.empty();
        }
        Platform.runLater(() -> {
            List<User> userList = users.get();
            for (User user : userList) {
                // Put names into VBox
                for (int i = 0; i < 40; i++) {
                    Label usernameLabel = new Label(user.getUsername());
                    usersVBox.getChildren().add(usernameLabel);
                }
            }
        });
        return users;
    }

    private Optional<List<User>> handleUsersException(Throwable ex) {
        ex.printStackTrace();
        return Optional.empty();
    }

    @FXML
    private void onConfirmButtonClicked() {
        confirmDialogActionListener.onConfirmCustomRoleUpdate(customRole);
    }

    @FXML
    private void onCancelButtonClicked() {
        confirmDialogActionListener.onCancelCustomRoleUpdate();
    }
}
