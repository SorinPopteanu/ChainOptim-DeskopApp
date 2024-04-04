package org.chainoptim.desktop.shared.table.edit.cells;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

import java.util.List;

public class EditableCell<S, T> extends TableCell<S, T> {

    private final BooleanProperty isEditMode;
    private TextField textField;
    private final List<Integer> editableRows;

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
        textField.setOnAction(e -> commitEdit((T) textField.getText()));
        textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (Boolean.FALSE.equals(newValue)) {
                commitEdit((T) textField.getText());
            }
        });
    }
}
