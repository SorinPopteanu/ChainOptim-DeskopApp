package org.chainoptim.desktop.shared.search.filters;

import org.chainoptim.desktop.shared.common.uielements.UIItem;
import org.chainoptim.desktop.shared.enums.FilterType;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import jfxtras.scene.control.LocalDateTimeTextField;

import java.util.List;

public class FilterBar extends HBox {

    private List<FilterOption> filterOptions;
    private SearchParams searchParams;

    private final ComboBox<UIItem> keyComboBox = new ComboBox<>();
    private final LocalDateTimeTextField dateTimePicker = new LocalDateTimeTextField();
    private final HBox numberPicker = new HBox();
    private final ComboBox<UIItem> enumComboBox = new ComboBox<>();

    public void initializeFilterBar(List<FilterOption> filterOptions, SearchParams searchParams) {
        this.filterOptions = filterOptions;
        this.searchParams = searchParams;
        initializeUI();
    }

    private void initializeUI() {
        configureComboBoxes();
        showFilterValuePicker(null); // Hide all filter value pickers at first

        setUpKeyComboBox();

        setUpEnumPicker();

        setUpNumberPicker();

        setUpDatePicker();

        this.getChildren().addAll(keyComboBox, enumComboBox, dateTimePicker, numberPicker);
    }

    private void setUpKeyComboBox() {
        keyComboBox.setPromptText("Filter by...");
        keyComboBox.getItems().addAll(filterOptions.stream().map(FilterOption::getKey).toList());

        keyComboBox.valueProperty().addListener((observable, oldValue, newValue) ->
                handleFilterTypeChange(newValue)
        );
    }

    private void handleFilterTypeChange(UIItem newValue) {
        FilterOption correspondingFilterOption = filterOptions.stream()
                .filter(filterOption -> filterOption.getKey().equals(newValue))
                .findFirst()
                .orElseThrow();
        showFilterValuePicker(correspondingFilterOption.getFilterType());

        if (correspondingFilterOption.getFilterType() == FilterType.ENUM) {
            enumComboBox.getItems().clear();
            enumComboBox.getItems().addAll(correspondingFilterOption.getValueOptions());
        }
    }

    private void setUpEnumPicker() {
        enumComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                searchParams.getFiltersProperty().clear();
                searchParams.getFiltersProperty().put(keyComboBox.getValue().getValue(), newVal.getValue());
            }
        });
    }

    private void setUpNumberPicker() {
        TextField textField = new TextField();
        textField.setPromptText("Enter a number");
        textField.getStyleClass().setAll("custom-text-field");

        Button applyNumberPicker = new Button("Apply");
        applyNumberPicker.setOnAction(event -> {
            searchParams.getFiltersProperty().clear();
            searchParams.getFiltersProperty().put(keyComboBox.getValue().getValue(), textField.getText());
        });

        numberPicker.getChildren().addAll(textField, applyNumberPicker);
    }

    private void setUpDatePicker() {
        dateTimePicker.localDateTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                searchParams.getFiltersProperty().clear();
                searchParams.getFiltersProperty().put(keyComboBox.getValue().getValue(), dateTimePicker.getLocalDateTime().toString());
            }
        });
    }

    private void showFilterValuePicker(FilterType type) {
        toggleNodeVisibility(dateTimePicker, type == FilterType.DATE);
        toggleNodeVisibility(numberPicker, type == FilterType.NUMBER);
        toggleNodeVisibility(enumComboBox, type == FilterType.ENUM);
    }

    // Utils
    private void configureComboBoxes() {
        keyComboBox.setCellFactory(lv -> new ListCell<UIItem>() {
            @Override
            protected void updateItem(UIItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getLabel());
            }
        });

        keyComboBox.setButtonCell(new ListCell<UIItem>() {
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

        enumComboBox.setCellFactory(lv -> new ListCell<UIItem>() {
            @Override
            protected void updateItem(UIItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getLabel());
            }
        });

        enumComboBox.setButtonCell(new ListCell<UIItem>() {
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

    private void toggleNodeVisibility(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }
}
