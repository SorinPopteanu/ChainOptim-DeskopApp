package org.chainoptim.desktop.core.tenant.user.controller;

import org.chainoptim.desktop.core.tenant.customrole.model.CustomRole;
import org.chainoptim.desktop.core.tenant.user.model.User;
import org.chainoptim.desktop.core.tenant.user.service.UserService;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

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

    private Result<List<User>> handleUsersResponse(Result<List<User>> users) {
        if (users.getError() != null) {
            return new Result<>();
        }
        Platform.runLater(() -> {
            List<User> userList = users.getData();
            usersVBox.getChildren().clear();

            for (User user : userList) {
                // Put names into VBox
                Label usernameLabel = new Label(user.getUsername());
                usersVBox.getChildren().add(usernameLabel);
            }
        });
        return users;
    }

    private Result<List<User>> handleUsersException(Throwable ex) {
        ex.printStackTrace();
        return new Result<>();
    }

}
