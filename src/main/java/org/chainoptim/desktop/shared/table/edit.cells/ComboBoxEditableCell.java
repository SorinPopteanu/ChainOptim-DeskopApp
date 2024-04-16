package org.chainoptim.desktop.shared.table.edit.cells;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.ComboBox;
import java.util.List;

public abstract class ComboBoxEditableCell<S, T> extends EditableCell<S,T> {
    private final ComboBox<T> comboBox;

    public ComboBoxEditableCell (BooleanProperty isEditMode, List<Integer> editableRows, List<T> comboBoxItems) {
        super(isEditMode, editableRows);
        this.comboBox = new ComboBox<>();
        this.comboBox.getItems().addAll(comboBoxItems);
        this.comboBox.setOnAction(e -> commitEdit(this.comboBox.getValue()));
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            if (isEditMode.get() && editableRows.contains(getIndex())) {
                comboBox.setValue(item);
                setGraphic(comboBox);
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
    }

    @Override
    public void commitEdit(String newValue) {}

    public abstract void commitEdit(T newValue);
}
