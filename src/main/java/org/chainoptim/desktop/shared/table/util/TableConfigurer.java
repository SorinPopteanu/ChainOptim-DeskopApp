package org.chainoptim.desktop.shared.table.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

public class TableConfigurer {

    public static <T> void configureTableView(TableView<T> tableView, TableColumn<T, Boolean> selectRowColumn) {
        tableView.setMaxHeight(Double.MAX_VALUE);
        tableView.setEditable(true);
        tableView.getColumns().forEach(column -> column.setEditable(false));

        selectRowColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectRowColumn));
        selectRowColumn.setEditable(true);

        tableView.setColumnResizePolicy(param -> Boolean.TRUE);
    }
}
