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
//    private final PaginatedResults paginatedResults;
private long totalItems;

    @Inject
    public PageSelectorController(
            SearchParams searchParams
           // PaginatedResults paginatedResults
    ) {
        this.searchParams = searchParams;
        //this.paginatedResults = paginatedResults;
    }

    @FXML
    public void initialize(long totalItems) {
        int itemsPerPage = searchParams.getItemsPerPage();
        //long totalItems = paginatedResults.getTotalCount();
        this.totalItems = totalItems;
        int pageCount = (int) Math.ceil((double) totalItems / itemsPerPage);
        System.out.println("Total items: " + totalItems + " Items per page: " + itemsPerPage + " Page count: " + pageCount);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(searchParams.getPage() - 1);
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            System.out.println("Page changed to: " + newIndex);
            searchParams.setPage(newIndex.intValue() + 1);
        });
    }


}
