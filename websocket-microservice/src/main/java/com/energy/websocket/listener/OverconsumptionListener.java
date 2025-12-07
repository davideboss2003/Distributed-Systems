package com.energy.websocket.listener;

import com.energy.websocket.dto.NotificationMessage;
import com.energy.websocket.dto.OverconsumptionEvent;
import com.energy.websocket.handler.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener pentru evenimente de overconsumption din RabbitMQ
 * 
 * FLOW:
 * 1. Monitoring MS detectează overconsumption
 * 2. Monitoring MS publică în RabbitMQ (overconsumption_queue)
 * 3. ACEST LISTENER primește mesajul
 * 4. Convertește în NotificationMessage
 * 5. Apelează WebSocket handler pentru broadcast
 * 6. Toate browser-ele conectate primesc notificarea!
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OverconsumptionListener {
    
    private final NotificationWebSocketHandler webSocketHandler;
    
    /**
     * Ascultă queue-ul "overconsumption_queue"
     * Această metodă e apelată AUTOMAT când vine un mesaj nou în queue
     */
    @RabbitListener(queues = "${app.rabbitmq.overconsumption-queue}")
    public void onOverconsumptionEvent(OverconsumptionEvent event) {
        log.info("🚨 Received overconsumption event: device={}, consumption={} kWh > {} kWh",
                 event.getDeviceId(), event.getConsumption(), event.getMaxConsumption());
        
        // Convertește event-ul în NotificationMessage
        NotificationMessage notification = NotificationMessage.overconsumption(
            event.getDeviceId(),
            event.getUserId(),
            event.getConsumption(),
            event.getMaxConsumption(),
            event.getTimestamp()
        );
        
        // Broadcast la toate sesiunile WebSocket active
        webSocketHandler.broadcast(notification);
        
        log.info(" Notification broadcasted to {} clients", 
                 webSocketHandler.getActiveConnectionsCount());
    }
}
