package org.chainoptim.desktop.shared.common.uielements.settings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;

public class EnumSelector<T extends Enum<T>> extends HBox {

    private final ObjectProperty<T> selectedValueProperty = new SimpleObjectProperty<>();;
    private final Map<T, Button> buttons = new HashMap<>();

    public EnumSelector() {
        super();

        this.setAlignment(Pos.CENTER_LEFT);
    }

    public void initializeSelector(Class<T> enumType, T selectedValue) {
        this.selectedValueProperty.setValue(selectedValue);

        for (T value : enumType.getEnumConstants()) {
            // Create a button for each enum value
            Button button = new Button(value.toString());
            button.getStyleClass().setAll(
                    selectedValue.equals(value) ? "select-item-selected" : "select-item"
            );
            button.setOnAction(event -> selectValue(value));
            this.getChildren().add(button);
            buttons.put(value, button);
        }
    }

    public void selectValue(T value) {
        selectedValueProperty.setValue(value);
        buttons.forEach((k, v) -> v.getStyleClass().setAll(
                k.equals(selectedValueProperty.getValue()) ? "select-item-selected" : "select-item"
        ));
    }

    public ObjectProperty<T> getValueProperty() {
        return selectedValueProperty;
    }

}
