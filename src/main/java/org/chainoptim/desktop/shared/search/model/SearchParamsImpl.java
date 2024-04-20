package org.chainoptim.desktop.shared.search.model;

import javafx.beans.property.*;


public class SearchParamsImpl implements SearchParams {

    private final StringProperty searchQuery;
    private final StringProperty sortOption;
    private final BooleanProperty ascending;
    private final IntegerProperty page;
    private final IntegerProperty itemsPerPage;

    public SearchParamsImpl() {
        searchQuery = new SimpleStringProperty("");
        sortOption = new SimpleStringProperty("createdAt");
        ascending = new SimpleBooleanProperty(false);
        page = new SimpleIntegerProperty(1);
        itemsPerPage = new SimpleIntegerProperty(10);
    }

    public BooleanProperty getAscendingProperty() {
        return ascending;
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

    public void setSearchQuery(String searchQuery) {
        this.page.set(1);
        this.searchQuery.set(searchQuery);
    }
    public void setSortOption(String sortOption) {
        this.page.set(1);
        this.sortOption.set(sortOption);
    }
    public void setAscending(Boolean ascending) {
        this.page.set(1);
        this.ascending.set(ascending);
    }
    public void setPage(Integer page) {
        this.page.set(page);
    }
    public void setItemsPerPage(Integer itemsPerPage) {
        this.page.set(1);
        this.itemsPerPage.set(itemsPerPage);
    }

}