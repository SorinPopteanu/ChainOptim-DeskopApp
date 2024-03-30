package org.chainoptim.desktop.core.notification.service;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketServiceImpl extends WebSocketClient implements WebSocketService {

    public WebSocketServiceImpl(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void sendMessage(String message) {
        super.send(message);
    }

    @Override
    public void disconnect() {
        super.close();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("opened connection");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed connection");
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public void startWebSocketConnection() {
        URI uri;
        try {
            uri = new URI("ws://localhost:8080/ws");
            WebSocketServiceImpl webSocketClient = new WebSocketServiceImpl(uri);
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
