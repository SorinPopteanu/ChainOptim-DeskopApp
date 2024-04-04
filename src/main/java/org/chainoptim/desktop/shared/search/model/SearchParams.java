package org.chainoptim.desktop.shared.search.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public interface SearchParams {

    BooleanProperty getAscendingProperty();
    StringProperty getSearchQueryProperty();
    StringProperty getSortOptionProperty();
    IntegerProperty getPageProperty();
    IntegerProperty getItemsPerPageProperty();
    Boolean getAscending();
    String getSearchQuery();
    String getSortOption();
    Integer getPage();
    Integer getItemsPerPage();
    void setSearchQuery(String searchQuery);
    void setSortOption(String sortOption);
    void setAscending(Boolean ascending);
    void setPage(Integer page);
    void setItemsPerPage(Integer itemsPerPage);
}
