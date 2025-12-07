package com.energy.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pentru mesajele de notificare trimise prin WebSocket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    
    private String type;              // "OVERCONSUMPTION"
    private String deviceId;          // ID-ul device-ului
    private String userId;            // ID-ul user-ului (opțional)
    private String message;           // Mesaj human-readable
    private Double consumption;       // Consumul actual (kWh)
    private Double maxConsumption;    // Consumul maxim permis (kWh)
    private LocalDateTime timestamp;  // Când s-a întâmplat
    
    /**
     * Constructor convenience pentru overconsumption
     */
    public static NotificationMessage overconsumption(
            String deviceId, 
            String userId,
            Double consumption, 
            Double maxConsumption,
            LocalDateTime timestamp) {
        
        String message = String.format(
            "Device exceeded max consumption: %.2f kWh > %.2f kWh",
            consumption, maxConsumption
        );
        
        return new NotificationMessage(
            "OVERCONSUMPTION",
            deviceId,
            userId,
            message,
            consumption,
            maxConsumption,
            timestamp
        );
    }
}
