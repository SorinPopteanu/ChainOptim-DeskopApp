package org.chainoptim.desktop.shared.common.uielements.select;

import org.chainoptim.desktop.features.goods.unit.model.StandardUnit;
import org.chainoptim.desktop.features.goods.unit.model.UnitMagnitude;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

public class SelectUnitOfMeasurement extends HBox {

    private final ComboBox<StandardUnit> unitComboBox;
    private final ComboBox<UnitMagnitude> magnitudeComboBox;

    public SelectUnitOfMeasurement() {
        super();
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(10);

        unitComboBox = new ComboBox<>();
        unitComboBox.getStyleClass().add("custom-combo-box");
        unitComboBox.getItems().addAll(StandardUnit.values());
        unitComboBox.setCellFactory(lv -> new ListCell<StandardUnit>() {
            @Override
            protected void updateItem(StandardUnit item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName() + " (" + item.getAbbreviation() + ")");
            }
        });
        unitComboBox.setConverter(new StringConverter<StandardUnit>() {
            @Override
            public String toString(StandardUnit object) {
                return object == null ? "" : object.getName() + " (" + object.getAbbreviation() + ")";
            }

            @Override
            public StandardUnit fromString(String string) {
                return null; // No need to convert back
            }
        });

        magnitudeComboBox = new ComboBox<>();
        magnitudeComboBox.getStyleClass().add("custom-combo-box");
        magnitudeComboBox.getItems().addAll(UnitMagnitude.values());

        getChildren().addAll(unitComboBox, magnitudeComboBox);
    }

    public void initialize(StandardUnit unit, UnitMagnitude magnitude) {
        unitComboBox.getSelectionModel().select(unit);
        magnitudeComboBox.getSelectionModel().select(magnitude);
    }

    public StandardUnit getSelectedUnit() {
        return unitComboBox.getSelectionModel().getSelectedItem();
    }

    public UnitMagnitude getSelectedMagnitude() {
        return magnitudeComboBox.getSelectionModel().getSelectedItem();
    }
}
