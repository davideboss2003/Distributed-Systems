package com.energy.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.energy.websocket.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler pentru WebSocket connections
 * 
 * Acest handler gestionează:
 * 1. Conexiuni noi (afterConnectionEstablished)
 * 2. Deconectări (afterConnectionClosed)
 * 3. Trimitere notificări la toate sesiunile active (broadcast)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper;
    
    // Map pentru tracking toate sesiunile active
    // Key = session ID, Value = WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    /**
     * Apelat când un client se conectează
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info(" WebSocket connection established: {} (Total connections: {})", 
                 session.getId(), sessions.size());
    }
    
    /**
     * Apelat când un client se deconectează
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        log.info(" WebSocket connection closed: {} (Total connections: {})", 
                 session.getId(), sessions.size());
    }
    
    /**
     * Trimite o notificare la TOATE sesiunile active (broadcast)
     * 
     * @param notification Notificarea de trimis
     */
    public void broadcast(NotificationMessage notification) {
        log.info(" Broadcasting notification to {} clients: {}", 
                 sessions.size(), notification.getMessage());
        
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(notification);
        } catch (Exception e) {
            log.error(" Failed to serialize notification", e);
            return;
        }
        
        // Trimite la toate sesiunile active
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMessage));
                    log.debug(" Sent notification to session: {}", session.getId());
                } catch (IOException e) {
                    log.error(" Failed to send message to session: {}", session.getId(), e);
                }
            }
        });
    }
    
    /**
     * Returnează numărul de conexiuni active
     */
    public int getActiveConnectionsCount() {
        return sessions.size();
    }
}
