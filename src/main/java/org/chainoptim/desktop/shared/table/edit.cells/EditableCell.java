package org.chainoptim.desktop.shared.table.edit.cells;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EditableCell<S, T> extends TableCell<S, T> {

    protected final BooleanProperty isEditMode;
    private TextField textField;
    protected final List<Integer> editableRows;

    public EditableCell(BooleanProperty isEditMode, List<Integer> editableRows) {
        this.isEditMode = isEditMode;
        this.editableRows = editableRows;
        createTextField();
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            if (isEditMode.get() && editableRows.contains(getIndex())) {
                textField.setText(getString());
                setGraphic(textField);
                textField.selectAll();
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    protected String getString() {
        return getItem() == null ? "" : getItem().toString();
    }

    @Override
    public void startEdit() {
        if (!isEmpty() && isEditMode.get() && editableRows.contains(getIndex())) {
            super.startEdit();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getString());
        setGraphic(null);
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnAction(e -> {
            commitEdit(textField.getText());
        });
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && getIndex() >= 0){
                commitEdit(textField.getText());
            }
        });
    }

    public abstract void commitEdit(String newValue);

}
