package org.chainoptim.desktop.shared.common.ui.toast.controller;

import org.chainoptim.desktop.shared.common.ui.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Objects;

public class ToastController {

    // State
    private ToastInfo toastInfo;
    private Runnable closeCallback;

    // FXML
    @FXML
    private Circle outcomeIconWrapper;
    @FXML
    private Label outcomeIcon;
    @FXML
    private Label titleLabel;
    @FXML
    private TextFlow messageTextFlow;
    @FXML
    private Button closeButton;

    // Icons
    private Image closeIcon;
    private Image successIcon;
    private Image errorIcon;
    private Image infoIcon;
    private Image warningIcon;

    public void initialize(ToastInfo toastInfo, Runnable closeCallback) {
        this.toastInfo = toastInfo;
        this.closeCallback = closeCallback;

        initializeIcons();
        initializeToast();
    }

    private void initializeIcons() {
        closeIcon = new Image(Objects.requireNonNull(ToastController.class.getResourceAsStream("/img/xmark-solid.png")));
        successIcon = new Image(Objects.requireNonNull(ToastController.class.getResourceAsStream("/img/check-solid.png")));
        errorIcon = new Image(Objects.requireNonNull(ToastController.class.getResourceAsStream("/img/xmark-solid-white.png")));
        infoIcon = new Image(Objects.requireNonNull(ToastController.class.getResourceAsStream("/img/info-solid.png")));
        warningIcon = new Image(Objects.requireNonNull(ToastController.class.getResourceAsStream("/img/triangle-exclamation-solid-white.png")));
    }

    private void initializeToast() {
        initializeOutcomeIcon();

        titleLabel.setText(this.toastInfo.getTitle());
        Text text = new Text(this.toastInfo.getMessage());
        text.getStyleClass().setAll("toast-message");
        messageTextFlow.getChildren().add(text);

        initializeCloseButton();
    }

    private void initializeOutcomeIcon() {
        if (toastInfo == null) return;

        ImageView iconView = new ImageView();
        switch (toastInfo.getOperationOutcome()) {
            case OperationOutcome.SUCCESS -> {
                iconView.setImage(successIcon);
                outcomeIconWrapper.getStyleClass().setAll("success-outcome-icon-wrapper");
            }
            case OperationOutcome.ERROR -> {
                iconView.setImage(errorIcon);
                outcomeIconWrapper.getStyleClass().setAll("error-outcome-icon-wrapper");
            }
            case OperationOutcome.INFO -> {
                iconView.setImage(infoIcon);
                outcomeIconWrapper.getStyleClass().setAll("info-outcome-icon-wrapper");
            }
            case OperationOutcome.WARNING -> {
                iconView.setImage(warningIcon);
                outcomeIconWrapper.getStyleClass().setAll("warning-outcome-icon-wrapper");
            }
            default -> {}
        }
        iconView.setFitHeight(18);
        iconView.setFitWidth(18);

        outcomeIcon.setGraphic(iconView);
    }

    private void initializeCloseButton() {
        ImageView closeIconView = new ImageView(closeIcon);
        closeIconView.setFitHeight(14);
        closeIconView.setFitWidth(14);
        closeButton.setGraphic(closeIconView);
        closeButton.getStyleClass().setAll("no-style-button");

        closeButton.setOnAction(e -> closeCallback.run());
    }
}
