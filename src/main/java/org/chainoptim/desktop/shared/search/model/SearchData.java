package org.chainoptim.desktop.shared.search.model;

import org.chainoptim.desktop.shared.enums.SearchMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchData<T> {

    private T data;
    private SearchMode searchMode;
}
