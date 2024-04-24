package org.chainoptim.desktop.shared.table.edit.cell;

import javafx.beans.property.BooleanProperty;
import jfxtras.scene.control.LocalDateTimeTextField;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DateTimePickerCell<S, T> extends EditableCell<S, LocalDateTime> {
    private final LocalDateTimeTextField dateTimePicker;
    private final DateTimeFormatter formatter;
    private final boolean isEditable;

    protected DateTimePickerCell(BooleanProperty isEditMode, List<Integer> editableRows, boolean isEditable) {
        super(isEditMode, editableRows, LocalDateTime::parse);
        this.isEditable = isEditable;
        this.dateTimePicker = new LocalDateTimeTextField();
        this.formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");
        dateTimePicker.setDateTimeFormatter(formatter);

        dateTimePicker.localDateTimeProperty().addListener((obs, oldDateTime, newDateTime) -> {
            if (isEditable) {
                commitEdit(this.dateTimePicker.getLocalDateTime());
            }
        });
        this.dateTimePicker.setParseErrorCallback(e -> {
            System.out.println("DateTimePicker parsing error");
            return null;
        });
    }

    @Override
    protected void updateItem(LocalDateTime item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            if (isEditMode.get() && editableRows.contains(getIndex())) {
                dateTimePicker.setLocalDateTime(item);
                setGraphic(dateTimePicker);
                setText(null);
            } else {
                setText(item != null ? formatter.format(item) : null);
                setGraphic(null);
            }
        }
    }

    @Override
    public void startEdit() {
        if (isEditable && !isEmpty() && isEditMode.get() && editableRows.contains(getIndex())) {
            super.startEdit();
            dateTimePicker.setLocalDateTime(getItem());
            setText(null);
            setGraphic(dateTimePicker);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getString());
        setGraphic(null);
    }

    @Override
    protected void commitChange(S item, LocalDateTime newValue) {}

    @Override
    public void commitEdit(LocalDateTime newValue) {
        if (getIndex() >= 0) {
            super.commitEdit(newValue);
            S currentRowItem = getTableView().getItems().get(getIndex());
            commitChange(currentRowItem, newValue);
            updateItem(newValue, false);
        }
    }
}
