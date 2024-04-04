package org.chainoptim.desktop.shared.table.edit.cells;

import org.chainoptim.desktop.shared.table.model.TableData;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class EditableTextFieldTableCell<T> extends TableCell<TableData<T>, String> {

    private TextField textField;
    private String initialValue;
    private ChangeListener<Boolean> editListener;


    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
    }

    @Override
    public void startEdit() {
        String item = super.getTableView().getItems().get(super.getIndex()).getData().toString();

        boolean isInRange = super.getIndex() >= 0 && super.getIndex() < getTableView().getItems().size();
        if (!isInRange) return;

        TableData<T> order = getTableView().getItems().get(super.getIndex());

        if (editListener != null) {
            order.isSelectedProperty().removeListener(editListener);
        }

        editListener = (obs, wasSelected, isSelected) -> {
            if (Boolean.TRUE.equals(isSelected)) {
                textField = new TextField();
                textField.setEditable(true);
                textField.setText(item != null ? item : "");
                super.setGraphic(textField);
            } else {
                super.setGraphic(null);
            }
        };
        order.isSelectedProperty().addListener(editListener);
        editListener.changed(null, !order.isSelected(), order.isSelected());
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        super.setGraphic(null);
        super.setItem(initialValue);
        TableData<T> order = getTableView().getItems().get(getIndex());
        if (editListener != null) {
            order.isSelectedProperty().removeListener(editListener);
        }
    }

}
