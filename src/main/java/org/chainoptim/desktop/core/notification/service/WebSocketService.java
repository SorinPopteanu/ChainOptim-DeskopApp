package org.chainoptim.desktop.core.notification.service;

public interface WebSocketService {
    void connect();
    void sendMessage(String message);
    void disconnect();
    void startWebSocketConnection();
}
