package org.chainoptim.desktop.shared.table.model;

import javafx.beans.property.BooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class TableData<T> {

    private T data;
    private T oldData;
    private BooleanProperty isSelected;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getOldData() { return oldData; }

    public void setOldData(T oldData) { this.oldData = oldData; }

    public BooleanProperty isSelectedProperty() {
        return isSelected;
    }

    public void setSelectedProperty(BooleanProperty isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected.get();
    }

    public void setSelected(boolean isSelected) {
        this.isSelected.set(isSelected);
    }

}
