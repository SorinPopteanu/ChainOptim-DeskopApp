package org.chainoptim.desktop.shared.table.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.Region;

public class TableConfigurer {

    private TableConfigurer() {}

    public static <T> void configureTableView(TableView<T> tableView, TableColumn<T, Boolean> selectRowColumn) {
        tableView.setPrefHeight(Region.USE_COMPUTED_SIZE);
        tableView.setEditable(true);
        tableView.setEditable(true);
        tableView.getColumns().forEach(column -> column.setEditable(false));

        selectRowColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectRowColumn));
        selectRowColumn.setEditable(true);

        tableView.setColumnResizePolicy(param -> Boolean.TRUE);

        tableView.layout();
    }
}
