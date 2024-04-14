package org.chainoptim.desktop.shared.common.uielements.info;

import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.InfoLevel;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class InfoLabel extends Label {

    public InfoLabel(Feature feature, InfoLevel userSettingsLevel) {
        super();

        Image infoImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/circle-info-solid.png")));
        this.setGraphic(new ImageView(infoImage));

        FeatureInfo featureInfo = FeatureInfoMapper.getFeatureInfo(feature);

        Tooltip tooltip = new Tooltip(featureInfo.getTooltipText());
        Tooltip.install(this, tooltip);

        // Only show the info label if the feature info level is <= than the user settings level
        this.setVisible(featureInfo.getInfoLevel().compareTo(userSettingsLevel) <= 0);
    }
}
