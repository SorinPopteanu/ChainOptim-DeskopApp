package org.chainoptim.desktop.shared.table.edit.cell;

import org.chainoptim.desktop.shared.table.util.StringConverter;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

import java.util.List;

public abstract class EditableCell<S, T> extends TableCell<S, T> {

    protected final BooleanProperty isEditMode;
    private TextField textField;
    protected final List<Integer> editableRows;
    private final StringConverter<T> converter;

    protected EditableCell(BooleanProperty isEditMode, List<Integer> editableRows, StringConverter<T> converter) {
        this.isEditMode = isEditMode;
        this.editableRows = editableRows;
        this.converter = converter;
        createTextField();
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.getStyleClass().setAll("table-text-field");
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnAction(e -> {
            try {
                T convertedValue = converter.convert(textField.getText());
                commitEdit(convertedValue);
            } catch (Exception ex) {
                cancelEdit();
                ex.printStackTrace();
            }
        });
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (Boolean.FALSE.equals(isNowFocused)){
                try {
                    T convertedValue = converter.convert(textField.getText());
                    commitEdit(convertedValue);
                } catch (Exception e) {
                    cancelEdit();
                    e.printStackTrace();
                }
            }
        });
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
    public void commitEdit(T newValue) {
        if (getIndex() >= 0) {
            super.commitEdit(newValue);
            S currentRowItem = getTableView().getItems().get(getIndex());
            commitChange(currentRowItem, newValue);
            updateItem(newValue, false);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getString());
        setGraphic(null);
    }

    protected abstract void commitChange(S item, T newValue);

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
}
