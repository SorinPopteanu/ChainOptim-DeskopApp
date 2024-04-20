package org.chainoptim.desktop.shared.common.uielements.badge;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class FeatureCountBadge extends HBox {

    private final BadgeData badgeData;

    public FeatureCountBadge(BadgeData badgeData) {
        super();
        this.badgeData = badgeData;

        initializeBadge();
    }

    private void initializeBadge() {
        HBox badgeContainer = new HBox(0);
        badgeContainer.setAlignment(Pos.CENTER_LEFT);
        badgeContainer.getStyleClass().add("badge-container");

        Label featureLabel = new Label(badgeData.getFeatureName());
        featureLabel.getStyleClass().add("feature-count-label");

        // Separator region
        Region separator = new Region();
        separator.setPrefWidth(1);
        separator.setMinHeight(20);
        separator.setStyle("-fx-background-color: #d3d6d4;");

        Label countLabel = new Label(String.valueOf(badgeData.getCount()));
        countLabel.getStyleClass().add("count-label");

        badgeContainer.getChildren().addAll(featureLabel, separator, countLabel);
        badgeContainer.setOnMouseClicked(event -> badgeData.getOnBadgeClick().run());

        this.getChildren().add(badgeContainer);
    }
}
