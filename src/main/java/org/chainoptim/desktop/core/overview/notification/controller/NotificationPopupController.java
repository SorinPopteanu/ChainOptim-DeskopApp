package org.chainoptim.desktop.core.overview.notification.controller;

import org.chainoptim.desktop.core.overview.notification.model.Notification;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Objects;

public class NotificationPopupController {

    @FXML
    private TextFlow titleTextFlow;
    @FXML
    private Label titleLabel;
    @FXML
    private Button closeButton;

    public void initializePopup(Notification notification, Runnable closeAction) {
        titleLabel.setText(notification.getTitle());
        Text message = new Text(notification.getMessage());
        titleTextFlow.getChildren().add(message);

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/xmark-solid.png")));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(10);
        imageView.setFitWidth(10);
        closeButton.setGraphic(imageView);
        closeButton.setOnAction(e -> closeAction.run());
    }
}
