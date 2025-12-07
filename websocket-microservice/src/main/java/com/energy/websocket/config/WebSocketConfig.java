package com.energy.websocket.config;

import com.energy.websocket.handler.NotificationWebSocketHandler;
import com.energy.websocket.handler.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configurare WebSocket
 * 
 * Acest config face următoarele:
 * 1. Activează WebSocket support în Spring
 * 2. Registrează handler-ul la endpoint-ul /ws/notifications
 * 3. Registrează handler-ul la endpoint-ul /ws/chat
 * 4. Configurează CORS pentru a permite conexiuni de la frontend
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final NotificationWebSocketHandler notificationHandler;
    private final ChatWebSocketHandler chatHandler;
    
    @Value("${app.websocket.endpoint}")
    private String websocketEndpoint;
    
    @Value("${app.websocket.allowed-origins}")
    private String allowedOrigins;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
            .addHandler(notificationHandler, websocketEndpoint)
            .setAllowedOrigins(allowedOrigins.split(","));
        
        registry
            .addHandler(chatHandler, "/ws/chat")
            .setAllowedOrigins(allowedOrigins.split(","));
        
        System.out.println(" WebSocket handler registered at: " + websocketEndpoint);
        System.out.println(" Chat WebSocket handler registered at: /ws/chat");
        System.out.println(" Allowed origins: " + allowedOrigins);
    }
}
