package org.chainoptim.desktop.shared.common.ui.settings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.*;

public class EnumSelector<T extends Enum<T>> extends HBox {

    private final ObjectProperty<T> selectedValueProperty = new SimpleObjectProperty<>();;
    private final Map<T, Button> buttons = new LinkedHashMap<>();

    public EnumSelector() {
        super();

        this.setAlignment(Pos.CENTER_LEFT);
        this.setMaxWidth(Region.USE_PREF_SIZE);
        this.getStyleClass().setAll("enum-selector");
    }

    public void initializeSelector(Class<T> enumType, T selectedValue) {
        if (selectedValue == null) return;
        this.selectedValueProperty.setValue(selectedValue);

        int index = 0;
        for (T value : enumType.getEnumConstants()) {
            // Create a button for each enum value
            Button button = new Button(value.toString());

            styleButton(selectedValue, value, button, index, enumType);

            button.setOnAction(event -> selectValue(value, enumType));

            this.getChildren().add(button);
            buttons.put(value, button);

            index++;
        }
    }

    private void styleButton(T selectedValue, T value, Button button, int index, Class<T> enumType) {
        List<String> styles = new ArrayList<>();
        styles.add(selectedValue.equals(value) ? "enum-select-item-selected" : "enum-select-item");
        if (index == 0) {
            styles.add("enum-left-item");
        }
        if (index == enumType.getEnumConstants().length - 1) {
            styles.add("enum-right-item");
        }
        button.getStyleClass().setAll(styles);
    }

    public void selectValue(T value, Class<T> enumType) {
        selectedValueProperty.setValue(value);

        int index = 0;
        for (Map.Entry<T, Button> entry : buttons.entrySet()) {
            styleButton(value, entry.getKey(), entry.getValue(), index, enumType);
            index++;
        }
    }

    public ObjectProperty<T> getValueProperty() {
        return selectedValueProperty;
    }

}
