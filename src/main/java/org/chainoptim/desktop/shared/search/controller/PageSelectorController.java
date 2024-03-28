package org.chainoptim.desktop.shared.search.controller;

import com.google.inject.Inject;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class PageSelectorController {

    @FXML
    private Pagination pagination;
    private final SearchParams searchParams;

    @Inject
    public PageSelectorController(SearchParams searchParams) {
        this.searchParams = searchParams;
    }

    @FXML
    public void initialize(long totalItems) {
        int itemsPerPage = searchParams.getItemsPerPage();
        int pageCount = (int) Math.ceil((double) totalItems / itemsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(searchParams.getPage() - 1);
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
            searchParams.setPage(newIndex.intValue() + 1)
        );

    }

}
