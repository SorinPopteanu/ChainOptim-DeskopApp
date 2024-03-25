package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.organization.model.ConfirmDeleteDialogActionListener;
import org.chainoptim.desktop.core.organization.model.ConfirmUpdateDialogActionListener;
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

public class ConfirmCustomRoleDeleteController implements DataReceiver<Integer> {

    private final UserService userService;

    private Integer customRoleId;

    @Setter
    private ConfirmDeleteDialogActionListener actionListener;

    @FXML
    private VBox usersVBox;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    @Inject
    public ConfirmCustomRoleDeleteController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setData(Integer customRoleId) {
        this.customRoleId = customRoleId;

        userService.getUsersByCustomRoleId(customRoleId)
                .thenApply(this::handleUsersResponse)
                .exceptionally(this::handleUsersException);
    }

    private Optional<List<User>> handleUsersResponse(Optional<List<User>> users) {
        if (users.isEmpty()) {
            return Optional.empty();
        }
        Platform.runLater(() -> {
            List<User> userList = users.get();
            usersVBox.getChildren().clear();

            for (User user : userList) {
                // Put names into VBox
                Label usernameLabel = new Label(user.getUsername());
                usersVBox.getChildren().add(usernameLabel);
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
        actionListener.onConfirmCustomRoleDelete(customRoleId);
    }

    @FXML
    private void onCancelButtonClicked() {
        actionListener.onCancelCustomRoleDelete();
    }
}
