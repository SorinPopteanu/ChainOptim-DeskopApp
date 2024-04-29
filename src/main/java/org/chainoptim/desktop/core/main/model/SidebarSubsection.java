package org.chainoptim.desktop.core.main.model;

import javafx.beans.property.BooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SidebarSubsection {
    private String name;
    private String key;
    private String iconPath;
    private Runnable action;
    private BooleanProperty isSelected;

    public BooleanProperty getIsSelectedProperty() {
        return isSelected;
    }

    public boolean isSelected() {
        return isSelected.get();
    }

    public void setSelectedProperty(BooleanProperty isSelected) {
        this.isSelected = isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected.set(selected);
    }
}
