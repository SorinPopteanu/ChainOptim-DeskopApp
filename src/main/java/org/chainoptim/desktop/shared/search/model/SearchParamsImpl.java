package org.chainoptim.desktop.shared.search.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.Map;


public class SearchParamsImpl implements SearchParams {

    private final StringProperty searchQuery;
    private final ObservableMap<String, String> filters;
    private final StringProperty sortOption;
    private final BooleanProperty ascending;
    private final IntegerProperty page;
    private final IntegerProperty itemsPerPage;

    public SearchParamsImpl() {
        searchQuery = new SimpleStringProperty("");
        filters = new SimpleMapProperty<>(FXCollections.observableMap(new HashMap<>()));
        sortOption = new SimpleStringProperty("createdAt");
        ascending = new SimpleBooleanProperty(false);
        page = new SimpleIntegerProperty(1);
        itemsPerPage = new SimpleIntegerProperty(10);
    }

    public StringProperty getSearchQueryProperty() {
        return searchQuery;
    }
    public ObservableMap<String, String> getFiltersProperty() {
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
    public Map<String, String> getFilters() {
        return new HashMap<>(filters);
    }

    public void setSearchQuery(String searchQuery) {
        this.page.set(1);
        this.searchQuery.set(searchQuery);
    }
    public void setFilters(Map<String, String> filters) {
        this.page.set(1);
        this.filters.clear();
        this.filters.putAll(filters);
    }
    public void updateFilter(String key, String value) {
        this.filters.put(key, value);
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