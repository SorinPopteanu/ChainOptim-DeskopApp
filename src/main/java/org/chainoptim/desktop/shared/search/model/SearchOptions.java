package org.chainoptim.desktop.shared.search.model;

import org.chainoptim.desktop.shared.search.filters.FilterOption;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchOptions {

    private List<FilterOption> filterOptions;
    private Map<String, String> sortOptions;
}
