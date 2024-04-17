package org.chainoptim.desktop.shared.table.edit.cell;

import org.chainoptim.desktop.shared.table.util.StringConverter;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.ComboBox;
import java.util.List;

public abstract class ComboBoxEditableCell<S, T> extends EditableCell<S,T> {
    private final ComboBox<T> comboBox;

    protected ComboBoxEditableCell (BooleanProperty isEditMode, List<Integer> editableRows, StringConverter<T> converter, List<T> comboBoxItems) {
        super(isEditMode, editableRows, converter);
        this.comboBox = new ComboBox<>();
        this.comboBox.getStyleClass().setAll("table-combo-box");
        this.comboBox.getItems().addAll(comboBoxItems);
        this.comboBox.setOnAction(e -> commitEdit(this.comboBox.getValue()));
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            if (isEditMode.get() && editableRows.contains(getIndex())) {
                comboBox.setValue(item);
                setGraphic(comboBox);
                setText(null);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    @Override
    public void startEdit() {
        if (!isEmpty() && isEditMode.get() && editableRows.contains(getIndex())) {
            super.startEdit();
            comboBox.setValue(getItem());
            setText(null);
            setGraphic(comboBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getString());
        setGraphic(null);
        if (isEditMode.get() && editableRows.contains(getIndex())) {
            setGraphic(comboBox);
        } else {
            setGraphic(null);
        }
    }

    @Override
    public void commitEdit(T newValue) {
        super.commitEdit(newValue);
        setText(getItem().toString());
        setGraphic(comboBox);
    }
}
