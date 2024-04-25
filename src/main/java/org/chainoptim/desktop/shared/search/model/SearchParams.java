package org.chainoptim.desktop.shared.search.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;

import java.util.Map;

public interface SearchParams {

    StringProperty getSearchQueryProperty();
    ObservableMap<String, String> getFiltersProperty();
    StringProperty getSortOptionProperty();
    BooleanProperty getAscendingProperty();
    IntegerProperty getPageProperty();
    IntegerProperty getItemsPerPageProperty();
    String getSearchQuery();
    String getSortOption();
    Boolean getAscending();
    Integer getPage();
    Integer getItemsPerPage();
    void setSearchQuery(String searchQuery);
    void setFilters(Map<String, String> filters);
    void setSortOption(String sortOption);
    void setAscending(Boolean ascending);
    void setPage(Integer page);
    void setItemsPerPage(Integer itemsPerPage);
}
