package org.chainoptim.desktop.shared.common.ui.badge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BadgeData {

    private String featureName;
    private long count;
    private Runnable onBadgeClick;
}
