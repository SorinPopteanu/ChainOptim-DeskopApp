package org.chainoptim.desktop.shared.search.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;

import java.util.Map;

public interface SearchParams {

    StringProperty getSearchQueryProperty();
    StringProperty getSortOptionProperty();
    BooleanProperty getAscendingProperty();
    IntegerProperty getPageProperty();
    IntegerProperty getItemsPerPageProperty();
    ObservableMap<String, String> getFiltersProperty();
    String getSearchQuery();
    String getSortOption();
    Boolean getAscending();
    Integer getPage();
    Integer getItemsPerPage();
    Map<String, String> getFilters();
    void setSearchQuery(String searchQuery);
    void setSortOption(String sortOption);
    void setAscending(Boolean ascending);
    void setPage(Integer page);
    void setItemsPerPage(Integer itemsPerPage);
    void setFilters(Map<String, String> filters);
    void removeFilter(String key);
    void updateFilter(String key, String value);
}
