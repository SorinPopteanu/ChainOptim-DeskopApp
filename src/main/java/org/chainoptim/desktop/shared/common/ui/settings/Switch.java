package org.chainoptim.desktop.shared.common.ui.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import lombok.Getter;

@Getter
public class Switch extends HBox {

    private BooleanProperty isOn = new SimpleBooleanProperty();

    private Button leftButton;
    private Button rightButton;

    public void initializeSwitch(boolean isOn) {
        this.getStyleClass().setAll("enum-selector");
        this.setMaxWidth(Region.USE_PREF_SIZE);
        this.setMaxHeight(Region.USE_PREF_SIZE);

        leftButton = new Button("On");
        leftButton.setOnAction(event -> toggle(true));

        rightButton = new Button("Off");
        rightButton.setOnAction(event -> toggle(false));

        toggle(isOn);

        this.getChildren().addAll(leftButton, rightButton);
    }

    public void toggle(boolean isOn) {
        leftButton.getStyleClass().clear();
        leftButton.getStyleClass().add(isOn ? "enum-select-item-selected" : "enum-select-item");
        leftButton.getStyleClass().add("enum-left-item");
        rightButton.getStyleClass().clear();
        rightButton.getStyleClass().add(isOn ? "enum-select-item" : "enum-select-item-selected");
        rightButton.getStyleClass().add("enum-right-item");
        this.isOn.setValue(isOn);
    }

    public BooleanProperty getIsOnProperty() {
        return isOn;
    }
}
