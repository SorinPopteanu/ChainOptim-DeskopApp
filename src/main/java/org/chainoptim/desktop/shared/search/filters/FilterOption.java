package org.chainoptim.desktop.shared.search.filters;

import org.chainoptim.desktop.shared.common.ui.UIItem;
import org.chainoptim.desktop.shared.enums.FilterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterOption {

    private UIItem key;
    private List<UIItem> valueOptions;
    private FilterType filterType;
}
