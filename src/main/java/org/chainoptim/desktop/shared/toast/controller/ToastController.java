package org.chainoptim.desktop.shared.toast.controller;

import org.chainoptim.desktop.shared.toast.model.ToastInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ToastController {

    private ToastInfo toastInfo;

    @FXML
    private Label titleLabel;

    public void setToastInfo(ToastInfo toastInfo) {
        this.toastInfo = toastInfo;
        titleLabel.setText(toastInfo.getTitle());
    }
}
