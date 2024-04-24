package org.chainoptim.desktop.shared.search.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.Map;


public class SearchParamsImpl implements SearchParams {

    private final StringProperty searchQuery;
    private final MapProperty<String, String> filters;
    private final StringProperty sortOption;
    private final BooleanProperty ascending;
    private final IntegerProperty page;
    private final IntegerProperty itemsPerPage;

    public SearchParamsImpl() {
        searchQuery = new SimpleStringProperty("");
        filters = new SimpleMapProperty<>(FXCollections.observableHashMap());
        sortOption = new SimpleStringProperty("createdAt");
        ascending = new SimpleBooleanProperty(false);
        page = new SimpleIntegerProperty(1);
        itemsPerPage = new SimpleIntegerProperty(10);
    }

    public StringProperty getSearchQueryProperty() {
        return searchQuery;
    }
    public MapProperty<String, String> getFiltersProperty() {
        return filters;
    }
    public StringProperty getSortOptionProperty() {
        return sortOption;
    }
    public BooleanProperty getAscendingProperty() {
        return ascending;
    }
    public IntegerProperty getPageProperty() {
        return page;
    }
    public IntegerProperty getItemsPerPageProperty() {
        return itemsPerPage;
    }
    public String getSearchQuery() {
        return searchQuery.get();
    }
    public Map<String, String> getFilters() {
        return filters.get();
    }
    public String getSortOption() {
        return sortOption.get();
    }
    public Boolean getAscending () {
        return ascending.get();
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
    public void setFilters(Map<String, String> filters) {
        this.page.set(1);
        ObservableMap<String, String> observableFilters = FXCollections.observableMap(filters);
        this.filters.set(observableFilters);
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