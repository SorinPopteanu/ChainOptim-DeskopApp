package org.chainoptim.desktop.shared.search;

import org.chainoptim.desktop.features.factory.dto.FactoriesSearchDTO;
import org.chainoptim.desktop.shared.search.model.UIItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Map;

public class FilterBar extends HBox {

    private Map<UIItem, List<UIItem>> filterOptions;

    private ComboBox<UIItem> filterComboBox;
    private ComboBox<UIItem> valueComboBox;

    public void initializeFilterBar(Map<UIItem, List<UIItem>> filterOptions) {
        this.filterOptions = filterOptions;
        initializeUI();
    }

    private void initializeUI() {
        filterComboBox = new ComboBox<>();
        valueComboBox = new ComboBox<>();

        configureComboBoxes();

        filterComboBox.setPromptText("Filter by...");
        filterComboBox.getItems().addAll(filterOptions.keySet());

        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            valueComboBox.getItems().clear();
            valueComboBox.getItems().addAll(filterOptions.get(newValue));
            valueComboBox.setVisible(true);
            valueComboBox.setManaged(true);
        });
        valueComboBox.setVisible(false);
        valueComboBox.setManaged(false);

        this.getChildren().addAll(filterComboBox, valueComboBox);
    }

    private void configureComboBoxes() {
        filterComboBox.setCellFactory(lv -> new ListCell<UIItem>() {
            @Override
            protected void updateItem(UIItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getLabel());
            }
        });

        filterComboBox.setButtonCell(new ListCell<UIItem>() {
            @Override
            protected void updateItem(UIItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLabel());
                }
            }
        });

        valueComboBox.setCellFactory(lv -> new ListCell<UIItem>() {
            @Override
            protected void updateItem(UIItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getLabel());
            }
        });

        valueComboBox.setButtonCell(new ListCell<UIItem>() {
            @Override
            protected void updateItem(UIItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLabel());
                }
            }
        });
    }
}
