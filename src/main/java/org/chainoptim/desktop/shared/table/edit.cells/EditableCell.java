package org.chainoptim.desktop.shared.table.edit.cells;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EditableCell<S, T> extends TableCell<S, T> {

    private final BooleanProperty isEditMode;
    private TextField textField;
    private final List<Integer> editableRows;
    private T oldValue;

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

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }

    @Override
    public void startEdit() {
        if (!isEmpty() && isEditMode.get() && editableRows.contains(getIndex())) {
            oldValue = getItem();
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
            System.out.println("Committing edit: " + textField.getText());
        });
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && getIndex() >= 0){
                commitEdit(textField.getText());
                System.out.println("Committing edit on focus: " + textField.getText());
            }
        });
    }

    public abstract void commitEdit(String newValue);

}
