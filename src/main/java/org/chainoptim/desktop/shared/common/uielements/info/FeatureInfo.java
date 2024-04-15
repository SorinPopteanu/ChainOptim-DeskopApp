package org.chainoptim.desktop.shared.common.uielements.info;

import org.chainoptim.desktop.shared.enums.InfoLevel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeatureInfo {

    private final String tooltipText;
    private final InfoLevel infoLevel;
}
