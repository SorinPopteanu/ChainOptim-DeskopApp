package org.chainoptim.desktop.core.overview.controller;

import org.chainoptim.desktop.core.overview.model.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class TimelineController {

    private List<Event> events;

    @FXML
    private Button refreshButton;
    @FXML
    private HBox eventsHBox;

    public void initialize() {
        eventsHBox.setSpacing(40);
        eventsHBox.setStyle("-fx-background-color: #f4f4f4; -fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 8px;");

        events = new ArrayList<>();
        events.add(new Event("Supply Order Arrival", LocalDateTime.now().plusDays(1), "Expected to arrive tomorrow"));
        events.add(new Event("Inventory Restock", LocalDateTime.now().plusDays(3), "New stock of products arriving"));
        events.add(new Event("Monthly Review Meeting", LocalDateTime.now().plusDays(5), "Discuss supply chain performance"));
        events.add(new Event("Supply Order Arrival", LocalDateTime.now().plusDays(1), "Expected to arrive tomorrow"));
        events.add(new Event("Inventory Restock", LocalDateTime.now().plusDays(3), "New stock of products arriving"));
        events.add(new Event("Monthly Review Meeting", LocalDateTime.now().plusDays(5), "Discuss supply chain performance"));
        events.add(new Event("Supply Order Arrival", LocalDateTime.now().plusDays(1), "Expected to arrive tomorrow"));
        events.add(new Event("Inventory Restock", LocalDateTime.now().plusDays(3), "New stock of products arriving"));
        events.add(new Event("Monthly Review Meeting", LocalDateTime.now().plusDays(5), "Discuss supply chain performance"));
        events.add(new Event("Supply Order Arrival", LocalDateTime.now().plusDays(1), "Expected to arrive tomorrow"));
        events.add(new Event("Inventory Restock", LocalDateTime.now().plusDays(3), "New stock of products arriving"));
        events.add(new Event("Monthly Review Meeting", LocalDateTime.now().plusDays(5), "Discuss supply chain performance"));

        renderTimeline();
    }

    private void renderTimeline() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

        for (Event event : events) {
            VBox eventBox = createEventBox(event, formatter);
            eventsHBox.getChildren().add(eventBox);
        }
    }

    private VBox createEventBox(Event event, DateTimeFormatter formatter) {
        VBox eventBox = new VBox();
        eventBox.setAlignment(Pos.CENTER);
        eventBox.setPrefWidth(150);

        // Dot to indicate the event
        Region dot = new Region();
        dot.setPrefSize(10, 10);
        dot.setStyle("-fx-background-color: #0078d4; -fx-background-radius: 5;"); // Blue dot

        // Title
        Label title = new Label(event.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        // Date and time
        Label dateTime = new Label(event.getDateTime().format(formatter));
        dateTime.setStyle("-fx-text-fill: gray;");

        // Message
        Label message = new Label(event.getMessage());
        message.setStyle("-fx-text-fill: #555;");

        // Add components to the event box
        eventBox.getChildren().addAll(dot, title, dateTime, message);
        return eventBox;
    }

    @FXML
    private void refreshEvents() {
        System.out.println("Refreshing events...");
    }
}
