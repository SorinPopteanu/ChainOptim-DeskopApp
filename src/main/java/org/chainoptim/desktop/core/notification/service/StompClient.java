package org.chainoptim.desktop.core.notification.service;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StompClient {

    private final String url = "ws://localhost:8080/ws";

    public void connect() {
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(transports));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandlerAdapter sessionHandler = new MySessionHandler();
        ListenableFuture<StompSession> futureSession = stompClient.connect(url, sessionHandler);


        try {
            StompSession stompSession = futureSession.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private class MySessionHandler extends StompSessionHandlerAdapter {

    }
}
