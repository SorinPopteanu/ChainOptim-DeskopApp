package org.chainoptim.desktop.shared.common.uielements.info;

import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.InfoLevel;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class InfoLabel extends Label {

    public InfoLabel() {
        super();

        Image infoImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/circle-info-solid.png")));
        ImageView imageView = new ImageView(infoImage);
        imageView.setFitHeight(15);
        imageView.setFitWidth(15);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0.15);
        imageView.setEffect(colorAdjust);
        this.setGraphic(imageView);

        this.setVisible(false);
        this.setManaged(false);
    }

    public void setFeatureAndLevel(Feature feature, InfoLevel userSettingsLevel) {
        FeatureInfo featureInfo = FeatureInfoMapper.getFeatureInfo(feature);
        if (featureInfo == null) return;
        if (userSettingsLevel == null) return;

        CustomTooltip customTooltip = new CustomTooltip(featureInfo.getTooltipText());
        customTooltip.attachToNode(this);

        // Only show the info label if the feature info level is <= than the user settings level
        boolean shouldBeVisible = featureInfo.getInfoLevel().compareTo(userSettingsLevel) <= 0;
        this.setVisible(shouldBeVisible);
        this.setManaged(shouldBeVisible);
    }
}
