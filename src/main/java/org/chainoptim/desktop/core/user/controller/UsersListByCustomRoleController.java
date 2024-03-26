package org.chainoptim.desktop.core.user.controller;

import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.core.user.service.UserService;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class UsersListByCustomRoleController implements DataReceiver<CustomRole> {

    private final UserService userService;

    private CustomRole customRole;

    @FXML
    private VBox usersVBox;

    @Inject
    public UsersListByCustomRoleController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setData(CustomRole data) {
        customRole = data;

        userService.getUsersByCustomRoleId(customRole.getId())
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

}
