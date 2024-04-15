package org.chainoptim.desktop.core.settings.model;

import org.chainoptim.desktop.shared.enums.InfoLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralSettings {

    private InfoLevel infoLevel;

    public GeneralSettings deepCopy() {
        return new GeneralSettings(infoLevel);
    }

}
