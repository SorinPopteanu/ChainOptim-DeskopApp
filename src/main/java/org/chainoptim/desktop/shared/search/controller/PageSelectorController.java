package org.chainoptim.desktop.shared.search.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import org.chainoptim.desktop.shared.search.model.SearchParams;

public class PageSelectorController {

    @FXML
    private Pagination pagination;

    @FXML
    public void initialize(SearchParams searchParams, long totalItems) {
        int itemsPerPage = searchParams.getItemsPerPage();
        int pageCount = (int) Math.ceil((double) totalItems / itemsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(searchParams.getPage() - 1);
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
            searchParams.setPage(newIndex.intValue() + 1)
        );

    }

}
