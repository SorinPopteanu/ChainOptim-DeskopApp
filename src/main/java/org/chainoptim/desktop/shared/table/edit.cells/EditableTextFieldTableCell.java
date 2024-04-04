package org.chainoptim.desktop.shared.table.edit.cells;

import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

public class EditableTextFieldTableCell extends TableCell<SupplierOrder, String> {

    private TextField textField;
    private String initialValue;

    public EditableTextFieldTableCell() {
    }



}
