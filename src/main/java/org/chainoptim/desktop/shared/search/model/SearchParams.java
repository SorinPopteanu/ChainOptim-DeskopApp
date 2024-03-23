package org.chainoptim.desktop.shared.search.model;

import javafx.beans.property.*;
import lombok.Getter;
import lombok.Setter;


public class SearchParams {

    private final StringProperty searchQuery;
    private final StringProperty sortOption;
    private final BooleanProperty ascending;
    private final IntegerProperty page;
    private final IntegerProperty itemsPerPage;

    public SearchParams() {
        searchQuery = new SimpleStringProperty("");
        sortOption = new SimpleStringProperty("createdAt");
        ascending = new SimpleBooleanProperty(true);
        page = new SimpleIntegerProperty(1);
        itemsPerPage = new SimpleIntegerProperty(10);
    }

    public BooleanProperty getAscendingProperty() {
        return ascending;
    }
    public Boolean getAscending () {
        return ascending.get();
    }
    public String getSearchQuery() {
        return searchQuery.get();
    }
    public String getSortOption() {
        return sortOption.get();
    }
    public Integer getPage() {
        return page.get();
    }
    public Integer getItemsPerPage() {
        return itemsPerPage.get();
    }
    public StringProperty getSearchQueryProperty() {
        return searchQuery;
    }
    public StringProperty getSortOptionProperty() {
        return sortOption;
    }
    public IntegerProperty getPageProperty() {
        return page;
    }
    public IntegerProperty getItemsPerPageProperty() {
        return itemsPerPage;
    }
    public void setSearchQuery(String searchQuery) {
        this.searchQuery.set(searchQuery);
    }
    public void setSortOption(String sortOption) {
        this.sortOption.set(sortOption);
    }
    public void setAscending(Boolean ascending) {
        this.ascending.set(ascending);
    }
    public void setPage(Integer page) {
        this.page.set(page);
    }
    public void setItemsPerPage(Integer itemsPerPage) {
        this.itemsPerPage.set(itemsPerPage);
    }

}