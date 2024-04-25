package org.chainoptim.desktop.shared.common.uielements.settings;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import lombok.Getter;

@Getter
public class Switch extends HBox {

    private boolean isOn;

    private Button leftButton;
    private Button rightButton;

    public void initializeSwitch(boolean isOn) {
        this.getStyleClass().setAll("custom-switch");
        this.setMaxWidth(Region.USE_PREF_SIZE);

        leftButton = new Button("On");
        leftButton.setStyle("-fx-border-radius: 5 0 0 5; -fx-background-radius: 5 0 0 5; -fx-border-width: 1 0 1 1;");
        leftButton.setOnAction(event -> toggle(true));

        rightButton = new Button("Off");
        rightButton.setStyle("-fx-border-radius: 0 5 5 0; -fx-background-radius: 0 5 5 0; -fx-border-width: 1 1 1 0;");
        rightButton.setOnAction(event -> toggle(false));

        toggle(isOn);

        this.getChildren().addAll(leftButton, rightButton);
    }

    private void toggle(boolean isOn) {
        leftButton.getStyleClass().setAll(isOn ? "switch-select-item-selected" : "switch-select-item");
        rightButton.getStyleClass().setAll(isOn ? "switch-select-item" : "switch-select-item-selected");
        this.isOn = isOn;
    }
}
