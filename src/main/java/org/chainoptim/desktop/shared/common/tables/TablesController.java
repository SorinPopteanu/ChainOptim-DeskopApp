package org.chainoptim.desktop.shared.common.tables;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;

public class TablesController<T> {

    @FXML
    private TableView<T> tableView;

    public void setColumns(Map<String, String> columnsMapping) {
        tableView.getColumns().clear();
        for (Map.Entry<String, String> entry : columnsMapping.entrySet()) {
            TableColumn<T, Object> column = new TableColumn<>(entry.getKey());
            column.setCellValueFactory(new PropertyValueFactory<>(entry.getValue()));
            tableView.getColumns().add(column);
        }

    }

    public void setData(List<T> data) {
        tableView.getItems().setAll(data);
    }
}
