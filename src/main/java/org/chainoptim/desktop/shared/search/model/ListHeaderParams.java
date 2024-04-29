package org.chainoptim.desktop.shared.search.model;

import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.search.filters.FilterOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListHeaderParams {
    SearchMode searchMode;
    SearchParams searchParams;
    String titleText;
    String titleIconPath;
    Feature feature;
    Map<String, String> sortOptionsMap;
    List<FilterOption> filterOptions;
    Runnable refreshAction;
    String createNewItemButtonText;
    String createNewItem;
}
