package org.chainoptim.desktop.shared.common.uielements.info;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;

import java.util.Objects;

public class CustomTooltip extends Popup {

    public CustomTooltip(String text) {
        super();

        Text textNode = new Text(text);
        textNode.getStyleClass().add("custom-tooltip-text");

        TextFlow content = new TextFlow();
        content.setMaxWidth(600);
        content.getStyleClass().add("custom-tooltip");
        content.getChildren().add(textNode);

        VBox wrapper = new VBox(content);
        getContent().add(wrapper);

        wrapper.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/common-elements.css")).toExternalForm());
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
