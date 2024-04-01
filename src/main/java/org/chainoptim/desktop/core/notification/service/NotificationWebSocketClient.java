package org.chainoptim.desktop.core.notification.service;

import org.chainoptim.desktop.core.notification.model.Notification;
import org.chainoptim.desktop.core.notification.model.NotificationUser;
import org.chainoptim.desktop.shared.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class NotificationWebSocketClient extends WebSocketClient {

    private Consumer<Notification> messageConsumer;

    public NotificationWebSocketClient(URI serverUri, Consumer<Notification> messageConsumer) {
        super(serverUri);
        this.messageConsumer = messageConsumer;
    }

    public NotificationWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("opened connection");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);

        try {
            // Deserialize into Notification
            Notification notification = JsonUtil.getObjectMapper().readValue(message, Notification.class);

            // Update UI
            if (messageConsumer != null) {
                Platform.runLater(() -> messageConsumer.accept(notification));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed connection");
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
