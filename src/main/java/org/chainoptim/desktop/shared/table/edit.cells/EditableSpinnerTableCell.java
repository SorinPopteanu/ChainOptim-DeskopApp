package org.chainoptim.desktop.shared.table.edit.cells;

import javafx.scene.control.*;
import javafx.util.StringConverter;

public class EditableSpinnerTableCell<S, T extends Number> extends TableCell<S, T> {
    private Spinner<T> spinner;
    private T initialValue;
    private final StringConverter<T> converter;

    public EditableSpinnerTableCell(StringConverter<T> converter) {
        this.converter = converter;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (spinner != null) {
                    spinner.getValueFactory().setValue(item);
                }
                setText(null);
                setGraphic(spinner);
            } else {
                setText(converter.toString(item));
                setGraphic(null);
            }
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();

        if (isEmpty()) {
            return;
        }

        if (spinner == null) {
            createSpinner();
        }

        spinner.getValueFactory().setValue(getItem());
        setText(null);
        setGraphic(spinner);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(converter.toString(getItem()));
        setGraphic(null);
    }

    @Override
    public void commitEdit(T newValue) {
        super.commitEdit(newValue);
        commitEditValue(newValue);
    }

    private void createSpinner() {
        spinner = new Spinner<>();
        spinner.setEditable(true);
        spinner.setValueFactory(new SpinnerValueFactory() {
            @Override
            public void decrement(int steps) {
                if (getValue() instanceof Integer) {
                    setValue((T) Integer.valueOf(((Integer) getValue()).intValue() - steps));
                } else if (getValue() instanceof Double) {
                    setValue((T) Double.valueOf(((Double) getValue()).doubleValue() - steps));
                } else if (getValue() instanceof Float) {
                    setValue((T) Float.valueOf(((Float) getValue()).floatValue() - steps));
                }
            }

            @Override
            public void increment(int steps) {
                if (getValue() instanceof Integer) {
                    setValue((T) Integer.valueOf(((Integer) getValue()).intValue() + steps));
                } else if (getValue() instanceof Double) {
                    setValue((T) Double.valueOf(((Double) getValue()).doubleValue() + steps));
                } else if (getValue() instanceof Float) {
                    setValue((T) Float.valueOf(((Float) getValue()).floatValue() + steps));
                }
            }
        });
    }

    private void commitEditValue(T newValue) {
        commitEdit(newValue);
        if (getTableRow() != null) {
            ((TableView<S>) getTableView()).edit(getTableRow().getIndex(), getTableColumn());
        }
    }
}
