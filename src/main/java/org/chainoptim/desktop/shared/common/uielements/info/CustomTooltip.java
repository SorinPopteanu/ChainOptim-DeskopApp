package org.chainoptim.desktop.shared.common.uielements.info;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Popup;

import java.util.Objects;

public class CustomTooltip extends Popup {

    public CustomTooltip(String text) {
        super();
        Label content = new Label(text);
        content.getStyleClass().add("custom-tooltip");
        getContent().add(content);
        content.autosize();

        content.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/common-elements.css")).toExternalForm());
    }

    public void attachToNode(Node node) {
        node.setOnMouseEntered(e -> {
            double x = e.getScreenX() - 10;
            double y = e.getScreenY() + 15;
            show(node, x, y);
        });

        node.setOnMouseExited(e -> hide());
    }
}
